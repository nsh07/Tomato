/*
 * Copyright (c) 2025 Nishant Mishra
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

import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SliderState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nsh07.pomodoro.TomatoApplication
import org.nsh07.pomodoro.billing.BillingManager
import org.nsh07.pomodoro.data.AppPreferenceRepository
import org.nsh07.pomodoro.data.TimerRepository
import org.nsh07.pomodoro.service.ServiceHelper
import org.nsh07.pomodoro.ui.Screen
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerAction
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerMode
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerState
import org.nsh07.pomodoro.utils.millisecondsToStr

@OptIn(FlowPreview::class, ExperimentalMaterial3Api::class)
class SettingsViewModel(
    private val billingManager: BillingManager,
    private val preferenceRepository: AppPreferenceRepository,
    private val serviceHelper: ServiceHelper,
    private val time: MutableStateFlow<Long>,
    private val timerRepository: TimerRepository,
    private val timerState: MutableStateFlow<TimerState>
) : ViewModel() {
    val backStack = mutableStateListOf<Screen.Settings>(Screen.Settings.Main)

    val isPlus = billingManager.isPlus
    val serviceRunning = timerRepository.serviceRunning.asStateFlow()

    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState = _settingsState.asStateFlow()

    val focusTimeTextFieldState by lazy {
        TextFieldState((timerRepository.focusTime / 60000).toString())
    }
    val shortBreakTimeTextFieldState by lazy {
        TextFieldState((timerRepository.shortBreakTime / 60000).toString())
    }
    val longBreakTimeTextFieldState by lazy {
        TextFieldState((timerRepository.longBreakTime / 60000).toString())
    }

    val sessionsSliderState by lazy {
        SliderState(
            value = timerRepository.sessionLength.toFloat(),
            steps = 4,
            valueRange = 1f..6f,
            onValueChangeFinished = ::updateSessionLength
        )
    }

    private var focusFlowCollectionJob: Job? = null
    private var shortBreakFlowCollectionJob: Job? = null
    private var longBreakFlowCollectionJob: Job? = null

    init {
        viewModelScope.launch {
            reloadSettings()
        }
    }

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.SaveAlarmSound -> saveAlarmSound(action.uri)
            is SettingsAction.SaveAlarmEnabled -> saveAlarmEnabled(action.enabled)
            is SettingsAction.SaveVibrateEnabled -> saveVibrateEnabled(action.enabled)
            is SettingsAction.SaveDndEnabled -> saveDndEnabled(action.enabled)
            is SettingsAction.SaveColorScheme -> saveColorScheme(action.color)
            is SettingsAction.SaveTheme -> saveTheme(action.theme)
            is SettingsAction.SaveBlackTheme -> saveBlackTheme(action.enabled)
            is SettingsAction.SaveAodEnabled -> saveAodEnabled(action.enabled)
        }
    }

    private fun updateSessionLength() {
        viewModelScope.launch {
            timerRepository.sessionLength = preferenceRepository.saveIntPreference(
                "session_length",
                sessionsSliderState.value.toInt()
            )
            refreshTimer()
        }
    }

    fun runTextFieldFlowCollection() {
        focusFlowCollectionJob = viewModelScope.launch(Dispatchers.IO) {
            snapshotFlow { focusTimeTextFieldState.text }
                .debounce(500)
                .collect {
                    if (it.isNotEmpty()) {
                        timerRepository.focusTime = it.toString().toLong() * 60 * 1000
                        refreshTimer()
                        preferenceRepository.saveIntPreference(
                            "focus_time",
                            timerRepository.focusTime.toInt()
                        )
                    }
                }
        }
        shortBreakFlowCollectionJob = viewModelScope.launch(Dispatchers.IO) {
            snapshotFlow { shortBreakTimeTextFieldState.text }
                .debounce(500)
                .collect {
                    if (it.isNotEmpty()) {
                        timerRepository.shortBreakTime = it.toString().toLong() * 60 * 1000
                        refreshTimer()
                        preferenceRepository.saveIntPreference(
                            "short_break_time",
                            timerRepository.shortBreakTime.toInt()
                        )
                    }
                }
        }
        longBreakFlowCollectionJob = viewModelScope.launch(Dispatchers.IO) {
            snapshotFlow { longBreakTimeTextFieldState.text }
                .debounce(500)
                .collect {
                    if (it.isNotEmpty()) {
                        timerRepository.longBreakTime = it.toString().toLong() * 60 * 1000
                        refreshTimer()
                        preferenceRepository.saveIntPreference(
                            "long_break_time",
                            timerRepository.longBreakTime.toInt()
                        )
                    }
                }
        }
    }

    fun cancelTextFieldFlowCollection() {
        if (!serviceRunning.value) serviceHelper.startService(TimerAction.ResetTimer)
        focusFlowCollectionJob?.cancel()
        shortBreakFlowCollectionJob?.cancel()
        longBreakFlowCollectionJob?.cancel()
    }

    private fun saveAlarmEnabled(enabled: Boolean) {
        viewModelScope.launch {
            timerRepository.alarmEnabled = enabled
            _settingsState.update { currentState ->
                currentState.copy(alarmEnabled = enabled)
            }
            preferenceRepository.saveBooleanPreference("alarm_enabled", enabled)
        }
    }

    private fun saveVibrateEnabled(enabled: Boolean) {
        viewModelScope.launch {
            timerRepository.vibrateEnabled = enabled
            _settingsState.update { currentState ->
                currentState.copy(vibrateEnabled = enabled)
            }
            preferenceRepository.saveBooleanPreference("vibrate_enabled", enabled)
        }
    }

    private fun saveDndEnabled(enabled: Boolean) {
        viewModelScope.launch {
            timerRepository.dndEnabled = enabled
            _settingsState.update { currentState ->
                currentState.copy(dndEnabled = enabled)
            }
            preferenceRepository.saveBooleanPreference("dnd_enabled", enabled)
        }
    }

    private fun saveAlarmSound(uri: Uri?) {
        viewModelScope.launch {
            timerRepository.alarmSoundUri = uri
            _settingsState.update { currentState ->
                currentState.copy(alarmSound = uri.toString())
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

    suspend fun reloadSettings() {
        val theme = preferenceRepository.getStringPreference("theme")
            ?: preferenceRepository.saveStringPreference("theme", "auto")
        val colorScheme = preferenceRepository.getStringPreference("color_scheme")
            ?: preferenceRepository.saveStringPreference("color_scheme", Color.White.toString())
        val blackTheme = preferenceRepository.getBooleanPreference("black_theme")
            ?: preferenceRepository.saveBooleanPreference("black_theme", false)
        val aodEnabled = preferenceRepository.getBooleanPreference("aod_enabled")
            ?: preferenceRepository.saveBooleanPreference("aod_enabled", false)
        val alarmSound = preferenceRepository.getStringPreference("alarm_sound")
            ?: preferenceRepository.saveStringPreference(
                "alarm_sound",
                (Settings.System.DEFAULT_ALARM_ALERT_URI
                    ?: Settings.System.DEFAULT_RINGTONE_URI).toString()
            )
        val alarmEnabled = preferenceRepository.getBooleanPreference("alarm_enabled")
            ?: preferenceRepository.saveBooleanPreference("alarm_enabled", true)
        val vibrateEnabled = preferenceRepository.getBooleanPreference("vibrate_enabled")
            ?: preferenceRepository.saveBooleanPreference("vibrate_enabled", true)
        val dndEnabled = preferenceRepository.getBooleanPreference("dnd_enabled")
            ?: preferenceRepository.saveBooleanPreference("dnd_enabled", false)

        _settingsState.update { currentState ->
            currentState.copy(
                theme = theme,
                colorScheme = colorScheme,
                alarmSound = alarmSound,
                blackTheme = blackTheme,
                aodEnabled = aodEnabled,
                alarmEnabled = alarmEnabled,
                vibrateEnabled = vibrateEnabled,
                dndEnabled = dndEnabled
            )
        }
    }

    private fun refreshTimer() {
        if (!serviceRunning.value) {
            time.update { timerRepository.focusTime }

            timerState.update { currentState ->
                currentState.copy(
                    timerMode = TimerMode.FOCUS,
                    timeStr = millisecondsToStr(time.value),
                    totalTime = time.value,
                    nextTimerMode = if (timerRepository.sessionLength > 1) TimerMode.SHORT_BREAK else TimerMode.LONG_BREAK,
                    nextTimeStr = millisecondsToStr(if (timerRepository.sessionLength > 1) timerRepository.shortBreakTime else timerRepository.longBreakTime),
                    currentFocusCount = 1,
                    totalFocusCount = timerRepository.sessionLength
                )
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as TomatoApplication)
                val appBillingManager = application.container.billingManager
                val appPreferenceRepository = application.container.appPreferenceRepository
                val appTimerRepository = application.container.appTimerRepository
                val serviceHelper = application.container.serviceHelper
                val time = application.container.time
                val timerState = application.container.timerState

                SettingsViewModel(
                    billingManager = appBillingManager,
                    preferenceRepository = appPreferenceRepository,
                    serviceHelper = serviceHelper,
                    time = time,
                    timerRepository = appTimerRepository,
                    timerState = timerState
                )
            }
        }
    }
}
