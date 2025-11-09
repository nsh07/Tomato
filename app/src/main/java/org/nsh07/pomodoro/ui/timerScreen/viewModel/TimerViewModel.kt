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

package org.nsh07.pomodoro.ui.timerScreen.viewModel

import android.provider.Settings
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nsh07.pomodoro.TomatoApplication
import org.nsh07.pomodoro.data.PreferenceRepository
import org.nsh07.pomodoro.data.Stat
import org.nsh07.pomodoro.data.StatRepository
import org.nsh07.pomodoro.data.TimerRepository
import org.nsh07.pomodoro.service.ServiceHelper
import org.nsh07.pomodoro.utils.millisecondsToStr
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@OptIn(FlowPreview::class)
class TimerViewModel(
    private val preferenceRepository: PreferenceRepository,
    private val serviceHelper: ServiceHelper,
    private val statRepository: StatRepository,
    private val timerRepository: TimerRepository,
    private val _timerState: MutableStateFlow<TimerState>,
    private val _time: MutableStateFlow<Long>
) : ViewModel() {
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    val time: StateFlow<Long> = _time.asStateFlow()

    val progress = _time.combine(_timerState) { remainingTime, uiState ->
        (uiState.totalTime.toFloat() - remainingTime) / uiState.totalTime
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    private var cycles = 0

    private var startTime = 0L
    private var pauseTime = 0L
    private var pauseDuration = 0L

    init {
        if (!timerRepository.serviceRunning.value)
            viewModelScope.launch(Dispatchers.IO) {
                timerRepository.focusTime =
                    preferenceRepository.getIntPreference("focus_time")?.toLong()
                        ?: preferenceRepository.saveIntPreference(
                            "focus_time",
                            timerRepository.focusTime.toInt()
                        ).toLong()
                timerRepository.shortBreakTime =
                    preferenceRepository.getIntPreference("short_break_time")?.toLong()
                        ?: preferenceRepository.saveIntPreference(
                            "short_break_time",
                            timerRepository.shortBreakTime.toInt()
                        ).toLong()
                timerRepository.longBreakTime =
                    preferenceRepository.getIntPreference("long_break_time")?.toLong()
                        ?: preferenceRepository.saveIntPreference(
                            "long_break_time",
                            timerRepository.longBreakTime.toInt()
                        ).toLong()
                timerRepository.sessionLength =
                    preferenceRepository.getIntPreference("session_length")
                        ?: preferenceRepository.saveIntPreference(
                            "session_length",
                            timerRepository.sessionLength
                        )

                timerRepository.alarmEnabled =
                    preferenceRepository.getBooleanPreference("alarm_enabled")
                        ?: preferenceRepository.saveBooleanPreference("alarm_enabled", true)
                timerRepository.vibrateEnabled =
                    preferenceRepository.getBooleanPreference("vibrate_enabled")
                        ?: preferenceRepository.saveBooleanPreference("vibrate_enabled", true)
                timerRepository.dndEnabled =
                    preferenceRepository.getBooleanPreference("dnd_enabled")
                        ?: preferenceRepository.saveBooleanPreference("dnd_enabled", false)

                timerRepository.alarmSoundUri = (
                        preferenceRepository.getStringPreference("alarm_sound")
                            ?: preferenceRepository.saveStringPreference(
                                "alarm_sound",
                                (Settings.System.DEFAULT_ALARM_ALERT_URI
                                    ?: Settings.System.DEFAULT_RINGTONE_URI).toString()
                            )
                        ).toUri()

                _time.update { timerRepository.focusTime }
                cycles = 0
                startTime = 0L
                pauseTime = 0L
                pauseDuration = 0L

                _timerState.update { currentState ->
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

                var lastDate = statRepository.getLastDate()
                val today = LocalDate.now()

                // Fills dates between today and lastDate with 0s to ensure continuous history
                if (lastDate != null) {
                    while (ChronoUnit.DAYS.between(lastDate, today) > 0) {
                        lastDate = lastDate?.plusDays(1)
                        statRepository.insertStat(Stat(lastDate!!, 0, 0, 0, 0, 0))
                    }
                } else {
                    statRepository.insertStat(Stat(today, 0, 0, 0, 0, 0))
                }

                delay(1500)

                _timerState.update { currentState ->
                    currentState.copy(showBrandTitle = false)
                }
            }
    }

    fun onAction(action: TimerAction) {
        serviceHelper.startService(action)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as TomatoApplication)
                val appPreferenceRepository = application.container.appPreferenceRepository
                val appStatRepository = application.container.appStatRepository
                val appTimerRepository = application.container.appTimerRepository
                val serviceHelper = application.container.serviceHelper
                val timerState = application.container.timerState
                val time = application.container.time

                TimerViewModel(
                    preferenceRepository = appPreferenceRepository,
                    serviceHelper = serviceHelper,
                    statRepository = appStatRepository,
                    timerRepository = appTimerRepository,
                    _timerState = timerState,
                    _time = time
                )
            }
        }
    }
}