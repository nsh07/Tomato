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

import android.R.attr.value
import android.net.Uri
import android.util.Log
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nsh07.pomodoro.TomatoApplication
import org.nsh07.pomodoro.billing.BillingManager
import org.nsh07.pomodoro.data.AppPreferenceRepository
import org.nsh07.pomodoro.data.TimerRepository
import org.nsh07.pomodoro.ui.Screen

@OptIn(FlowPreview::class, ExperimentalMaterial3Api::class)
class SettingsViewModel(
    private val billingManager: BillingManager,
    private val preferenceRepository: AppPreferenceRepository,
    private val timerRepository: TimerRepository,
) : ViewModel() {
    val backStack = mutableStateListOf<Screen.Settings>(Screen.Settings.Main)

    val isPlus = billingManager.isPlus
    val isPurchaseStateLoaded = billingManager.isLoaded

    private val _isSettingsLoaded = MutableStateFlow(false)
    val isSettingsLoaded = _isSettingsLoaded.asStateFlow()

    private val _preferencesState = MutableStateFlow(PreferencesState())
    val preferencesState = _preferencesState.asStateFlow()

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

    val currentAlarmSound = timerRepository.alarmSoundUri.toString()

    private var focusFlowCollectionJob: Job? = null
    private var shortBreakFlowCollectionJob: Job? = null
    private var longBreakFlowCollectionJob: Job? = null

    val alarmSound =
        preferenceRepository.getStringPreferenceFlow("alarm_sound").distinctUntilChanged()
    val alarmEnabled =
        preferenceRepository.getBooleanPreferenceFlow("alarm_enabled").distinctUntilChanged()
    val vibrateEnabled =
        preferenceRepository.getBooleanPreferenceFlow("vibrate_enabled").distinctUntilChanged()
    val dndEnabled =
        preferenceRepository.getBooleanPreferenceFlow("dnd_enabled").distinctUntilChanged()

    init {
        viewModelScope.launch {
            val theme = preferenceRepository.getStringPreference("theme")
                ?: preferenceRepository.saveStringPreference("theme", "auto")
            val colorScheme = preferenceRepository.getStringPreference("color_scheme")
                ?: preferenceRepository.saveStringPreference("color_scheme", Color.White.toString())
            val blackTheme = preferenceRepository.getBooleanPreference("black_theme")
                ?: preferenceRepository.saveBooleanPreference("black_theme", false)
            val aodEnabled = preferenceRepository.getBooleanPreference("aod_enabled")
                ?: preferenceRepository.saveBooleanPreference("aod_enabled", false)
            val showClock = preferenceRepository.getStringPreference("show_clock")
                ?: preferenceRepository.saveStringPreference("show_clock", "Both")


            _preferencesState.update { currentState ->
                currentState.copy(
                    theme = theme,
                    colorScheme = colorScheme,
                    blackTheme = blackTheme,
                    aodEnabled = aodEnabled
                )
            }

            _preferencesState.update { currentState ->
                currentState.copy(
                    theme = theme,
                    colorScheme = colorScheme,
                    blackTheme = blackTheme,
                    aodEnabled = aodEnabled,
                    showClock = showClock
                )
            }

        }
    }

    private fun updateSessionLength() {
        viewModelScope.launch {
            timerRepository.sessionLength = preferenceRepository.saveIntPreference(
                "session_length",
                sessionsSliderState.value.toInt()
            )
        }
    }

    fun runTextFieldFlowCollection() {
        focusFlowCollectionJob = viewModelScope.launch(Dispatchers.IO) {
            snapshotFlow { focusTimeTextFieldState.text }
                .debounce(500)
                .collect {
                    if (it.isNotEmpty()) {
                        timerRepository.focusTime = it.toString().toLong() * 60 * 1000
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
                        preferenceRepository.saveIntPreference(
                            "long_break_time",
                            timerRepository.longBreakTime.toInt()
                        )
                    }
                }
        }
    }

    fun cancelTextFieldFlowCollection() {
        focusFlowCollectionJob?.cancel()
        shortBreakFlowCollectionJob?.cancel()
        longBreakFlowCollectionJob?.cancel()
    }

    fun saveAlarmEnabled(enabled: Boolean) {
        viewModelScope.launch {
            timerRepository.alarmEnabled = enabled
            preferenceRepository.saveBooleanPreference("alarm_enabled", enabled)
        }
    }

    fun saveVibrateEnabled(enabled: Boolean) {
        viewModelScope.launch {
            timerRepository.vibrateEnabled = enabled
            preferenceRepository.saveBooleanPreference("vibrate_enabled", enabled)
        }
    }

    fun saveDndEnabled(enabled: Boolean) {
        viewModelScope.launch {
            timerRepository.dndEnabled = enabled
            preferenceRepository.saveBooleanPreference("dnd_enabled", enabled)
        }
    }

    fun saveAlarmSound(uri: Uri?) {
        viewModelScope.launch {
            timerRepository.alarmSoundUri = uri
            preferenceRepository.saveStringPreference("alarm_sound", uri.toString())
        }
    }

    fun saveColorScheme(colorScheme: Color) {
        viewModelScope.launch {
            _preferencesState.update { currentState ->
                currentState.copy(colorScheme = colorScheme.toString())
            }
            preferenceRepository.saveStringPreference("color_scheme", colorScheme.toString())
        }
    }

    fun saveTheme(theme: String) {
        viewModelScope.launch {
            _preferencesState.update { currentState ->
                currentState.copy(theme = theme)
            }
            preferenceRepository.saveStringPreference("theme", theme)
        }
    }

    fun saveBlackTheme(blackTheme: Boolean) {
        viewModelScope.launch {
            _preferencesState.update { currentState ->
                currentState.copy(blackTheme = blackTheme)
            }
            preferenceRepository.saveBooleanPreference("black_theme", blackTheme)
        }
    }

    fun saveAodEnabled(aodEnabled: Boolean) {
        viewModelScope.launch {
            _preferencesState.update { currentState ->
                currentState.copy(aodEnabled = aodEnabled)
            }
            preferenceRepository.saveBooleanPreference("aod_enabled", aodEnabled)
        }
    }
    fun saveShowClock(showClock: String) {
        viewModelScope.launch {
            _preferencesState.update { currentState ->
                currentState.copy(showClock = showClock)
            }
            preferenceRepository.saveStringPreference("show_clock", showClock)
            timerRepository.showClock = showClock
            Log.d("SettingsViewModel", "saveShowClock saved=$value")

        }
    }






    fun resetPaywalledSettings() {
        _preferencesState.update { currentState ->
            currentState.copy(
                aodEnabled = false,
                blackTheme = false,
                colorScheme = Color.White.toString()
            )
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

        _preferencesState.update { currentState ->
            currentState.copy(
                theme = theme,
                colorScheme = colorScheme,
                blackTheme = blackTheme,
                aodEnabled = aodEnabled
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as TomatoApplication)
                val appPreferenceRepository = application.container.appPreferenceRepository
                val appTimerRepository = application.container.appTimerRepository
                val appBillingManager = application.container.billingManager

                SettingsViewModel(
                    billingManager = appBillingManager,
                    preferenceRepository = appPreferenceRepository,
                    timerRepository = appTimerRepository,
                )
            }
        }
    }
}
