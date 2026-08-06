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

package org.nsh07.pomodoro.timer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nsh07.pomodoro.data.StateRepository
import org.nsh07.pomodoro.service.TimerHelper
import org.nsh07.pomodoro.service.TimerManager
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerAction

class DesktopTimerHelper(
    private val timerManager: TimerManager,
    private val stateRepository: StateRepository
) : TimerHelper {
    private var job = SupervisorJob()
    private val timerScope = CoroutineScope(Dispatchers.IO + job)
    private val skipScope = CoroutineScope(Dispatchers.IO + job)

    private var autoAlarmStopScope: Job? = null

    private val _timerState by lazy { stateRepository.timerState }
    private val _settingsState by lazy { stateRepository.settingsState }

    private var mp3Player: MP3Player = MP3Player(null)

    override fun onAction(action: TimerAction) {
        if (action == TimerAction.ResetTimer)
            _timerState.update { currentState ->
                currentState.copy(serviceRunning = false)
            }
        else
            _timerState.update { currentState ->
                currentState.copy(serviceRunning = true)
            }

        when (action) {
            TimerAction.ResetTimer -> {
                if (_timerState.value.timerRunning) toggleTimer()
                skipScope.launch {
                    timerManager.resetTimer {}
                }
            }

            is TimerAction.SkipTimer -> skipScope.launch {
                timerManager.skipTimer(
                    onStart = { showTimerNotification(complete = false) },
                    onCompletion = { },
                    setDoNotDisturb = { }
                )
            }

            TimerAction.StopAlarm -> stopAlarm()

            TimerAction.ToggleTimer -> toggleTimer()

            TimerAction.UndoReset -> timerManager.undoReset()

            is TimerAction.SetInfiniteFocus -> {
                System.err.println("Invalid action: $action")
            }
        }
    }

    override fun updateWidgets() {
        // No-op for desktop
    }

    override fun updateWidget(appWidgetId: Int) {
        // No-op for desktop
    }

    private fun toggleTimer() {
        timerManager.toggleTimer(
            scope = timerScope,
            onPause = { showTimerNotification() },
            onStart = { },
            onTick = { _, updateNotification, _ ->
                if (updateNotification) {
                    showTimerNotification()
                }
            },
            onTimerExpired = {
                showTimerNotification(complete = true)
            },
            onSkipComplete = { },
            setDoNotDisturb = {},
            onStateChanged = {}
        )
    }

    private fun showTimerNotification(complete: Boolean = false) {
        if (complete) {
            startAlarm()
            _timerState.update { currentState ->
                currentState.copy(alarmRinging = true)
            }
        }
    }

    private fun startAlarm() {
        val settingsState = _settingsState.value
        if (mp3Player.audioPath != settingsState.alarmSoundUri) {
            // if the alarm file is changed in settings, recreate the MP3Player
            mp3Player.release()
            mp3Player = MP3Player(settingsState.alarmSoundUri)
        }

        if (settingsState.alarmEnabled) mp3Player.play()

        autoAlarmStopScope = CoroutineScope(Dispatchers.IO).launch {
            delay(1 * 60 * 1000)
            stopAlarm(fromAutoStop = true)
        }
    }

    private fun stopAlarm(fromAutoStop: Boolean = false) {
        val settingsState = _settingsState.value
        autoAlarmStopScope?.cancel()

        if (settingsState.alarmEnabled) {
            mp3Player.stop()
        }

//        activityCallbacks.activityTurnScreenOn(false)

        _timerState.update { currentState ->
            currentState.copy(alarmRinging = false)
        }

        showTimerNotification(complete = false)

        if (settingsState.autostartNextSession && !fromAutoStop)  // auto start next session
            toggleTimer()
    }
}
