/*
 * Copyright (c) 2026 Nishant Mishra
 *
 * This file is part of Tomato - a minimalist pomodoro timer for Android.
 *
 * Tomato is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Tomato is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Tomato.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.ui.settingsScreen.viewModel

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SliderState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.nsh07.pomodoro.billing.BillingManager
import org.nsh07.pomodoro.data.PreferenceRepository
import org.nsh07.pomodoro.data.StatRepository
import org.nsh07.pomodoro.data.StateRepository
import org.nsh07.pomodoro.data.Topic
import org.nsh07.pomodoro.data.Topic.Companion.defaultTopic
import org.nsh07.pomodoro.data.TopicRepository
import org.nsh07.pomodoro.data.TopicShape
import org.nsh07.pomodoro.service.TimerHelper
import org.nsh07.pomodoro.ui.Screen
import org.nsh07.pomodoro.ui.settingsScreen.components.MinuteInputs
import org.nsh07.pomodoro.ui.settingsScreen.components.minutesToMillisOrNull
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerAction
import org.nsh07.pomodoro.utils.logError
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
class SettingsViewModel(
    billingManager: BillingManager,
    private val preferenceRepository: PreferenceRepository,
    private val stateRepository: StateRepository,
    private val statRepository: StatRepository,
    private val topicRepository: TopicRepository,
    private val timerHelper: TimerHelper
) : ViewModel() {
    val backStack = mutableStateListOf<Screen.Settings>(Screen.Settings.Main)

    val isPlus = billingManager.isPlus
    val sessionActive = stateRepository.timerState
        .map { it.sessionActive }
        .distinctUntilChanged()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            stateRepository.timerState.value.sessionActive
        )

    private val isSessionActive: Boolean
        get() = stateRepository.timerState.value.sessionActive

    val currentTopicId = stateRepository.currentTopicId

    private val _settingsState = stateRepository.settingsState
    val settingsState = _settingsState.asStateFlow()

    private val _currentTopic = stateRepository.currentTopic

    private val _editingTopic = MutableStateFlow(_currentTopic.value)
    val editingTopic = _editingTopic.asStateFlow()

    /** Until the user picks a topic to edit, or edits one, the current topic is edited */
    private var editingTopicChosen = false

    var minuteInputs by mutableStateOf(MinuteInputs(_editingTopic.value))
        private set

    val sessionsSliderState by lazy {
        SliderState(
            value = _editingTopic.value.sessionLength.toFloat(),
            steps = 8,
            valueRange = 1f..10f,
            onValueChangeFinished = ::updateSessionLength
        )
    }

    val allTopics = topicRepository
        .getAllTopics()
        .map { it.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }) }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val editTopicMutex = Mutex()

    private var minutesWriteJob: Job? = null

    init {
        // the view model is created on startup, before the saved topic has been restored
        viewModelScope.launch {
            _currentTopic.collect { if (!editingTopicChosen) showTopic(it) }
        }
    }

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.SaveAlarmSound -> saveAlarmSound(action.uri)
            is SettingsAction.SaveAlarmEnabled -> saveAlarmEnabled(action.enabled)
            is SettingsAction.SaveVibrateEnabled -> saveVibrateEnabled(action.enabled)
            is SettingsAction.SaveDndEnabled -> saveDndEnabled(action.enabled)
            is SettingsAction.SaveMediaVolumeForAlarm -> saveMediaVolumeForAlarm(action.enabled)
            is SettingsAction.SaveSingleProgressBar -> saveSingleProgressBar(action.enabled)
            is SettingsAction.SaveAutostartNextSession -> saveAutostartNextSession(action.enabled)
            is SettingsAction.SaveSecureAod -> saveSecureAod(action.enabled)
            is SettingsAction.SaveColorScheme -> saveColorScheme(action.color)
            is SettingsAction.SaveTheme -> saveTheme(action.theme)
            is SettingsAction.SaveBlackTheme -> saveBlackTheme(action.enabled)
            is SettingsAction.SaveAodEnabled -> saveAodEnabled(action.enabled)

            is SettingsAction.SaveFocusGoal -> saveFocusGoal(action.goal)

            is SettingsAction.SaveVibrationOnDuration -> saveVibrationOnDuration(action.duration)
            is SettingsAction.SaveVibrationOffDuration -> saveVibrationOffDuration(action.duration)
            is SettingsAction.SaveVibrationAmplitude -> saveVibrationAmplitude(action.amplitude)

            is SettingsAction.CreateTopic -> createTopic(action.topic, action.setAsCurrent)
            is SettingsAction.DeleteTopic -> deleteTopic(action.topic, action.deleteStats)
            is SettingsAction.SetEditingTopic -> setEditingTopic(action.topic)
            is SettingsAction.SetEditingTopicName -> setEditingTopicName(action.name)
            is SettingsAction.SetEditingTopicMinutes -> setEditingTopicMinutes(action.minutes)
            is SettingsAction.SetEditingTopicColor -> setEditingTopicColor(action.color)
            is SettingsAction.SetEditingTopicShape -> setEditingTopicShape(action.shape)

            is SettingsAction.AskEraseData -> askEraseData()
            is SettingsAction.CancelEraseData -> cancelEraseData()
            is SettingsAction.EraseData -> deleteStats()
        }
    }

    private fun createTopic(topic: Topic, setAsCurrent: Boolean) {
        viewModelScope.launch {
            val id = topicRepository.insertTopic(topic)
            if (id == -1L) return@launch

            val created = topic.copy(id = id)
            setEditingTopic(created)

            if (setAsCurrent && !isSessionActive) {
                stateRepository.setTopic(created)
            }
        }
    }

    private fun deleteTopic(topic: Topic, deleteStats: Boolean) {
        if (topic.id == Topic.DEFAULT_TOPIC_ID) return
        viewModelScope.launch(Dispatchers.IO) {
            if (deleteStats) topicRepository.deleteTopic(topic)
            else topicRepository.deleteTopicMergingStats(topic, Topic.DEFAULT_TOPIC_ID)

            val fallback = topicRepository.getTopicById(Topic.DEFAULT_TOPIC_ID) ?: defaultTopic

            if (stateRepository.currentTopicId.value == topic.id) {
                stateRepository.setTopic(fallback)
            }
            if (_editingTopic.value.id == topic.id) setEditingTopic(fallback)
        }
    }

    fun setEditingTopic(topic: Topic) {
        editingTopicChosen = true
        showTopic(topic)
    }

    private fun showTopic(topic: Topic) {
        _editingTopic.update { topic }
        minuteInputs = MinuteInputs(topic)
        sessionsSliderState.value = topic.sessionLength.toFloat()
    }

    /**
     * Applies [transform] to the topic being edited and writes it to the database
     */
    private suspend fun editTopic(transform: (Topic) -> Topic) {
        editingTopicChosen = true
        editTopicMutex.withLock {
            val previous = _editingTopic.value
            val topic = _editingTopic.updateAndGet(transform)
            if (topic == previous) return

            if (!topicRepository.updateTopic(topic)) {
                // saving edit failed, roll back unless the topic being edited has since changed
                _editingTopic.update { if (it == topic) previous else it }
            }
        }
    }

    private fun setEditingTopicName(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            editTopic { it.copy(name = name) }
        }
    }

    private fun setEditingTopicColor(color: Color) {
        viewModelScope.launch(Dispatchers.IO) {
            editTopic { it.copy(color = color) }
        }
    }

    private fun setEditingTopicShape(shape: TopicShape) {
        viewModelScope.launch(Dispatchers.IO) {
            editTopic { it.copy(shape = shape) }
        }
    }

    private fun cancelEraseData() {
        viewModelScope.launch(Dispatchers.IO) {
            _settingsState.update { currentState ->
                currentState.copy(isShowingEraseDataDialog = false)
            }
        }
    }

    private fun askEraseData() {
        viewModelScope.launch(Dispatchers.IO) {
            _settingsState.update { currentState ->
                currentState.copy(isShowingEraseDataDialog = true)
            }
        }
    }

    private fun updateSessionLength() {
        viewModelScope.launch(Dispatchers.IO) {
            val value = sessionsSliderState.value.toInt()

            editTopic { it.copy(sessionLength = value) }
        }
    }

    private fun deleteStats() {
        viewModelScope.launch(Dispatchers.IO) {

            timerHelper.onAction(TimerAction.ResetTimer)
            statRepository.deleteAllStats()
            _settingsState.update {
                it.copy(isShowingEraseDataDialog = false)
            }
        }
    }

    private fun setEditingTopicMinutes(minutes: MinuteInputs) {
        // set right away, so that neither the restored topic nor the write interrupts typing
        editingTopicChosen = true
        minuteInputs = minutes
        minutesWriteJob?.cancel()
        minutesWriteJob = viewModelScope.launch(Dispatchers.IO) {
            delay(500.milliseconds)
            saveMinutes(minutes)
        }
    }

    /** Saves the durations in [minutes] that are valid, leaving the rest as they were */
    private suspend fun saveMinutes(minutes: MinuteInputs) {
        editTopic {
            it.copy(
                focusTime = minutes.focus.minutesToMillisOrNull() ?: it.focusTime,
                shortBreakTime = minutes.shortBreak.minutesToMillisOrNull() ?: it.shortBreakTime,
                longBreakTime = minutes.longBreak.minutesToMillisOrNull() ?: it.longBreakTime
            )
        }
    }

    fun onSettingsClosed() {
        // an edit still being debounced is saved rather than dropped
        if (minutesWriteJob?.isActive == true) {
            minutesWriteJob?.cancel()
            viewModelScope.launch(Dispatchers.IO) { saveMinutes(minuteInputs) }
        }

        if (!isSessionActive)
            try {
                timerHelper.onAction(TimerAction.ResetTimer)
            } catch (e: Exception) {
                logError(
                    "Service",
                    "Unable to start start service with action ResetTimer: ${e.message}"
                )
                e.printStackTrace()
            }
    }

    private fun saveFocusGoal(goal: Long) {
        viewModelScope.launch {
            _settingsState.update { currentState ->
                currentState.copy(focusGoal = goal)
            }
            preferenceRepository.saveIntPreference("focus_goal", goal.toInt())
        }
    }

    private fun saveAlarmEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _settingsState.update { currentState ->
                currentState.copy(alarmEnabled = enabled)
            }
            preferenceRepository.saveBooleanPreference("alarm_enabled", enabled)
        }
    }

    private fun saveVibrateEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _settingsState.update { currentState ->
                currentState.copy(vibrateEnabled = enabled)
            }
            preferenceRepository.saveBooleanPreference("vibrate_enabled", enabled)
        }
    }

    private fun saveDndEnabled(enabled: Boolean) {
        viewModelScope.launch {
            editTopic { it.copy(dndEnabled = enabled) }
        }
    }

    private fun saveAlarmSound(uri: String?) {
        viewModelScope.launch {
            _settingsState.update { currentState ->
                currentState.copy(alarmSoundUri = uri)
            }
            preferenceRepository.saveStringPreference("alarm_sound", uri.toString())
        }
    }

    private fun saveColorScheme(colorScheme: Color) {
        viewModelScope.launch {
            _settingsState.update { currentState ->
                currentState.copy(colorScheme = colorScheme)
            }
            preferenceRepository.saveColorPreference("color_scheme", colorScheme)
        }
    }

    private fun saveTheme(theme: String) {
        viewModelScope.launch {
            _settingsState.update { currentState ->
                currentState.copy(theme = theme)
            }
            preferenceRepository.saveStringPreference("theme", theme)
        }
    }

    private fun saveBlackTheme(blackTheme: Boolean) {
        viewModelScope.launch {
            _settingsState.update { currentState ->
                currentState.copy(blackTheme = blackTheme)
            }
            preferenceRepository.saveBooleanPreference("black_theme", blackTheme)
        }
    }

    private fun saveAodEnabled(aodEnabled: Boolean) {
        viewModelScope.launch {
            _settingsState.update { currentState ->
                currentState.copy(aodEnabled = aodEnabled)
            }
            preferenceRepository.saveBooleanPreference("aod_enabled", aodEnabled)
        }
    }

    private fun saveMediaVolumeForAlarm(mediaVolumeForAlarm: Boolean) {
        viewModelScope.launch {
            _settingsState.update { currentState ->
                currentState.copy(mediaVolumeForAlarm = mediaVolumeForAlarm)
            }
            preferenceRepository.saveBooleanPreference(
                "media_volume_for_alarm",
                mediaVolumeForAlarm
            )
        }
    }

    private fun saveSingleProgressBar(singleProgressBar: Boolean) {
        viewModelScope.launch {
            _settingsState.update { currentState ->
                currentState.copy(singleProgressBar = singleProgressBar)
            }
            preferenceRepository.saveBooleanPreference(
                "single_progress_bar",
                singleProgressBar
            )
        }
    }

    private fun saveAutostartNextSession(autostartNextSession: Boolean) {
        viewModelScope.launch {
            editTopic { it.copy(autostartNextSession = autostartNextSession) }
        }
    }

    private fun saveSecureAod(secureAod: Boolean) {
        viewModelScope.launch {
            _settingsState.update { currentState ->
                currentState.copy(secureAod = secureAod)
            }
            preferenceRepository.saveBooleanPreference(
                "secure_aod",
                secureAod
            )
        }
    }

    private fun saveVibrationOnDuration(vibrationOnDuration: Long) {
        viewModelScope.launch {
            _settingsState.update { currentState ->
                currentState.copy(vibrationOnDuration = vibrationOnDuration)
            }
            preferenceRepository.saveIntPreference(
                "vibration_on_duration",
                vibrationOnDuration.toInt()
            )
        }
    }

    private fun saveVibrationOffDuration(vibrationOffDuration: Long) {
        viewModelScope.launch {
            _settingsState.update { currentState ->
                currentState.copy(vibrationOffDuration = vibrationOffDuration)
            }
            preferenceRepository.saveIntPreference(
                "vibration_off_duration",
                vibrationOffDuration.toInt()
            )
        }
    }

    private fun saveVibrationAmplitude(vibrationAmplitude: Int) {
        viewModelScope.launch {
            _settingsState.update { currentState ->
                currentState.copy(vibrationAmplitude = vibrationAmplitude)
            }
            preferenceRepository.saveIntPreference(
                "vibration_amplitude",
                vibrationAmplitude
            )
        }
    }
}
