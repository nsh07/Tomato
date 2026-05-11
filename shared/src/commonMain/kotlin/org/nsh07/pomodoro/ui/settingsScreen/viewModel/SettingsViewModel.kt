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

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SliderState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nsh07.pomodoro.billing.BillingManager
import org.nsh07.pomodoro.data.PreferenceRepository
import org.nsh07.pomodoro.data.StatRepository
import org.nsh07.pomodoro.data.StateRepository
import org.nsh07.pomodoro.service.TimerHelper
import org.nsh07.pomodoro.ui.Screen
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerAction
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerMode
import org.nsh07.pomodoro.utils.logError
import org.nsh07.pomodoro.utils.millisecondsToStr

@OptIn(FlowPreview::class, ExperimentalMaterial3Api::class)
class SettingsViewModel(
    billingManager: BillingManager,
    private val preferenceRepository: PreferenceRepository,
    private val stateRepository: StateRepository,
    private val statRepository: StatRepository,
    private val timerHelper: TimerHelper
) : ViewModel() {
    private val time: MutableStateFlow<Long> = stateRepository.time

    val backStack = mutableStateListOf<Screen.Settings>(Screen.Settings.Main)

    val isPlus = billingManager.isPlus
    val serviceRunning = stateRepository.timerState
        .map { it.serviceRunning }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            false
        )

    private val _settingsState = stateRepository.settingsState
    val settingsState = _settingsState.asStateFlow()

    val focusTimeTextFieldState by lazy {
        TextFieldState((_settingsState.value.focusTime / 60000).toString())
    }
    val shortBreakTimeTextFieldState by lazy {
        TextFieldState((_settingsState.value.shortBreakTime / 60000).toString())
    }
    val longBreakTimeTextFieldState by lazy {
        TextFieldState((_settingsState.value.longBreakTime / 60000).toString())
    }

    val sessionsSliderState by lazy {
        SliderState(
            value = _settingsState.value.sessionLength.toFloat(),
            steps = 8,
            valueRange = 1f..10f,
            onValueChangeFinished = ::updateSessionLength
        )
    }

    private var focusFlowCollectionJob: Job? = null
    private var shortBreakFlowCollectionJob: Job? = null
    private var longBreakFlowCollectionJob: Job? = null

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

            is SettingsAction.AskEraseData -> askEraseData()
            is SettingsAction.CancelEraseData -> cancelEraseData()
            is SettingsAction.EraseData -> deleteStats()
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
            _settingsState.update { currentState ->
                currentState.copy(
                    sessionLength = preferenceRepository.saveIntPreference(
                        "session_length",
                        sessionsSliderState.value.toInt()
                    )
                )
            }
            refreshTimer()
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

    fun runTextFieldFlowCollection() {
        focusFlowCollectionJob = viewModelScope.launch(Dispatchers.IO) {
            snapshotFlow { focusTimeTextFieldState.text }
                .debounce(500)
                .collect {
                    if (it.isNotEmpty()) {
                        _settingsState.update { currentState ->
                            currentState.copy(focusTime = it.toString().toLong() * 60 * 1000)
                        }
                        refreshTimer()
                        preferenceRepository.saveIntPreference(
                            "focus_time",
                            _settingsState.value.focusTime.toInt()
                        )
                    }
                }
        }
        shortBreakFlowCollectionJob = viewModelScope.launch(Dispatchers.IO) {
            snapshotFlow { shortBreakTimeTextFieldState.text }
                .debounce(500)
                .collect {
                    if (it.isNotEmpty()) {
                        _settingsState.update { currentState ->
                            currentState.copy(shortBreakTime = it.toString().toLong() * 60 * 1000)
                        }
                        refreshTimer()
                        preferenceRepository.saveIntPreference(
                            "short_break_time",
                            _settingsState.value.shortBreakTime.toInt()
                        )
                    }
                }
        }
        longBreakFlowCollectionJob = viewModelScope.launch(Dispatchers.IO) {
            snapshotFlow { longBreakTimeTextFieldState.text }
                .debounce(500)
                .collect {
                    if (it.isNotEmpty()) {
                        _settingsState.update { currentState ->
                            currentState.copy(longBreakTime = it.toString().toLong() * 60 * 1000)
                        }
                        refreshTimer()
                        preferenceRepository.saveIntPreference(
                            "long_break_time",
                            _settingsState.value.longBreakTime.toInt()
                        )
                    }
                }
        }
    }

    fun cancelTextFieldFlowCollection() {
        if (!serviceRunning.value)
            try {
                timerHelper.onAction(TimerAction.ResetTimer)
            } catch (e: Exception) {
                logError(
                    "Service",
                    "Unable to start start service with action ResetTimer: ${e.message}"
                )
                e.printStackTrace()
            }
        focusFlowCollectionJob?.cancel()
        shortBreakFlowCollectionJob?.cancel()
        longBreakFlowCollectionJob?.cancel()
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
            _settingsState.update { currentState ->
                currentState.copy(dndEnabled = enabled)
            }
            preferenceRepository.saveBooleanPreference("dnd_enabled", enabled)
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
                currentState.copy(colorScheme = colorScheme.toString())
            }
            preferenceRepository.saveStringPreference("color_scheme", colorScheme.toString())
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
            _settingsState.update { currentState ->
                currentState.copy(autostartNextSession = autostartNextSession)
            }
            preferenceRepository.saveBooleanPreference(
                "autostart_next_session",
                autostartNextSession
            )
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

    private fun refreshTimer() {
        if (!serviceRunning.value) {
            val settingsState = _settingsState.value
            val infFocus = stateRepository.timerState.value.infiniteFocus

            if (!infFocus) time.update { settingsState.focusTime }

            if (!infFocus) stateRepository.timerState.update { currentState ->
                currentState.copy(
                    timerMode = TimerMode.FOCUS,
                    timeStr = millisecondsToStr(time.value),
                    totalTime = time.value,
                    nextTimerMode = if (settingsState.sessionLength > 1) TimerMode.SHORT_BREAK else TimerMode.LONG_BREAK,
                    nextTimeStr = millisecondsToStr(if (settingsState.sessionLength > 1) settingsState.shortBreakTime else settingsState.longBreakTime),
                    currentFocusCount = 1,
                    totalFocusCount = settingsState.sessionLength
                )
            }
        }
    }
}