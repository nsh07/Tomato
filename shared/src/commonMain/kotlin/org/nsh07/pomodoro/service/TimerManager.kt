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

package org.nsh07.pomodoro.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jetbrains.compose.resources.getString
import org.nsh07.pomodoro.data.StatRepository
import org.nsh07.pomodoro.data.StateRepository
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerMode
import org.nsh07.pomodoro.utils.millisecondsToStr
import tomato.shared.generated.resources.Res
import tomato.shared.generated.resources.infinite

class TimerManager(
    private val stateRepository: StateRepository,
    private val statRepository: StatRepository,
    private val currentTime: () -> Long,
) {
    private val _timerState by lazy { stateRepository.timerState }
    private val _settingsState by lazy { stateRepository.settingsState }
    private val _time = stateRepository.time

    /**
     * Remaining time
     */
    private var time: Long
        get() = _time.value
        set(value) = _time.update { value }

    var cycles = 0
    private var startTime = 0L
    private var pauseTime = 0L
    private var pauseDuration = 0L
    private var lastSavedDuration = 0L

    private val timerStateSnapshot by lazy { stateRepository.timerStateSnapshot }
    private val saveLock = Mutex()

    private var timerJob: Job? = null

    /**
     * Toggles the timer between running and paused states.
     *
     * Platform-specific operations (notifications, widgets, QS tiles) are handled via callbacks,
     * keeping the core timer logic multiplatform.
     *
     * @param scope The coroutine scope to launch the timer loop in
     * @param onPause Called when the timer is paused, with the remaining time
     * @param onStart Called when the timer is started/resumed
     * @param onTick Called on each timer tick with remaining time and flags for
     *   notification/widget updates
     * @param onTimerExpired Called when the timer reaches zero (before auto-skip)
     * @param onSkipComplete Called after auto-skip completes
     * @param setDoNotDisturb Called to enable/disable Do Not Disturb
     * @param onStateChanged Called after the toggle completes (e.g., to update QS tile)
     */
    fun toggleTimer(
        scope: CoroutineScope,
        onPause: (remainingTime: Long) -> Unit,
        onStart: () -> Unit,
        onTick: suspend (remainingTime: Long, updateNotification: Boolean, updateWidget: Boolean) -> Unit,
        onTimerExpired: suspend () -> Unit,
        onSkipComplete: suspend () -> Unit,
        setDoNotDisturb: (Boolean) -> Unit,
        onStateChanged: () -> Unit,
    ) {
        if (_timerState.value.timerRunning) {
            setDoNotDisturb(false)
            onPause(time)
            _timerState.update { currentState ->
                currentState.copy(timerRunning = false)
            }
            pauseTime = currentTime()
        } else {
            if (_timerState.value.timerMode == TimerMode.FOCUS) setDoNotDisturb(true)
            else setDoNotDisturb(false)
            onStart()
            _timerState.update { it.copy(timerRunning = true) }
            if (pauseTime != 0L) pauseDuration += currentTime() - pauseTime

            var iterations = -1
            var notificationUpdateCounter = -1

            timerJob = scope.launch {
                while (true) {
                    if (!_timerState.value.timerRunning) break
                    if (startTime == 0L) startTime = currentTime()

                    val settingsState = _settingsState.value
                    val timerState = _timerState.value

                    val focusTime =
                        if (!timerState.infiniteFocus) settingsState.focusTime else Long.MAX_VALUE
                    time = when (_timerState.value.timerMode) {
                        TimerMode.FOCUS -> focusTime - (currentTime() - startTime - pauseDuration)

                        TimerMode.SHORT_BREAK -> settingsState.shortBreakTime - (currentTime() - startTime - pauseDuration)

                        else -> settingsState.longBreakTime - (currentTime() - startTime - pauseDuration)
                    }

                    val freq = stateRepository.timerFrequency.toInt().coerceAtLeast(1)

                    iterations = (iterations + 1) % freq
                    notificationUpdateCounter =
                        (notificationUpdateCounter + 1) % (freq * 10) // update widget every 10 seconds

                    if (iterations == 0) {
                        onTick(time, true, notificationUpdateCounter == 0)
                    } else if (notificationUpdateCounter == 0) {
                        onTick(time, false, true)
                    }

                    if (time < 0) {
                        skipTimer(
                            onStart = onTimerExpired,
                            onCompletion = onSkipComplete,
                            setDoNotDisturb = setDoNotDisturb
                        )
                        _timerState.update { currentState ->
                            currentState.copy(timerRunning = false)
                        }
                        break
                    } else {
                        _timerState.update { currentState ->
                            currentState.copy(
                                timeStr = if (!currentState.infiniteFocus || currentState.timerMode != TimerMode.FOCUS)
                                    millisecondsToStr(time)
                                else millisecondsToStr(currentState.totalTime - time) // elapsed time
                            )
                        }
                        val totalTime = _timerState.value.totalTime

                        if (totalTime - time < lastSavedDuration)
                            lastSavedDuration =
                                0 // Sanity check, prevents bugs if service is force closed
                        if (totalTime - time - lastSavedDuration > 60000)
                            saveTimeToDb()
                    }

                    delay((1000f / stateRepository.timerFrequency).toLong())
                }
            }
        }

        onStateChanged()
    }

    suspend fun saveTimeToDb() {
        saveLock.withLock {
            val elapsedTime = _timerState.value.totalTime - time
            when (_timerState.value.timerMode) {
                TimerMode.FOCUS -> statRepository.addFocusTime(
                    (elapsedTime - lastSavedDuration).coerceAtLeast(0L)
                )

                else -> statRepository.addBreakTime(
                    (elapsedTime - lastSavedDuration).coerceAtLeast(0L)
                )
            }
            lastSavedDuration = elapsedTime
        }
    }

    suspend fun skipTimer(
        onStart: suspend () -> Unit,
        onCompletion: suspend () -> Unit,
        setDoNotDisturb: (Boolean) -> Unit
    ) {
        val settingsState = _settingsState.value
        saveTimeToDb()

        onStart()

        lastSavedDuration = 0
        startTime = 0L
        pauseTime = 0L
        pauseDuration = 0L

        cycles = (cycles + 1) % (settingsState.sessionLength * 2)

        if (cycles % 2 == 0) {
            _timerState.update { currentState ->
                if (currentState.timerRunning) setDoNotDisturb(true)
                time = if (!currentState.infiniteFocus) settingsState.focusTime else Long.MAX_VALUE

                currentState.copy(
                    timerMode = TimerMode.FOCUS,
                    timeStr = if (!currentState.infiniteFocus) millisecondsToStr(time)
                    else millisecondsToStr(0),
                    totalTime = time,
                    nextTimerMode = if (cycles == (settingsState.sessionLength - 1) * 2) TimerMode.LONG_BREAK else TimerMode.SHORT_BREAK,
                    nextTimeStr = if (cycles == (settingsState.sessionLength - 1) * 2) millisecondsToStr(
                        settingsState.longBreakTime
                    ) else millisecondsToStr(
                        settingsState.shortBreakTime
                    ),
                    currentFocusCount = cycles / 2 + 1,
                    totalFocusCount = settingsState.sessionLength
                )
            }
        } else {
            val long = cycles == (settingsState.sessionLength * 2) - 1
            time = if (long) settingsState.longBreakTime else settingsState.shortBreakTime

            _timerState.update { currentState ->
                if (currentState.timerRunning) setDoNotDisturb(false)

                currentState.copy(
                    timerMode = if (long) TimerMode.LONG_BREAK else TimerMode.SHORT_BREAK,
                    timeStr = millisecondsToStr(time),
                    totalTime = time,
                    nextTimerMode = TimerMode.FOCUS,
                    nextTimeStr = if (!currentState.infiniteFocus)
                        millisecondsToStr(settingsState.focusTime)
                    else getString(Res.string.infinite)
                )
            }
        }

        onCompletion()
    }

    suspend fun resetTimer(onCompletion: () -> Unit) {
        val settingsState = _settingsState.value
        val timerState = _timerState.value

        timerStateSnapshot.save(
            lastSavedDuration,
            time,
            cycles,
            startTime,
            pauseTime,
            pauseDuration,
            timerState
        )

        saveTimeToDb()
        lastSavedDuration = 0
        cycles = 0
        startTime = 0L
        pauseTime = 0L
        pauseDuration = 0L

        time = if (!timerState.infiniteFocus) settingsState.focusTime else Long.MAX_VALUE

        _timerState.update { currentState ->
            currentState.copy(
                timerMode = TimerMode.FOCUS,
                timeStr = if (!currentState.infiniteFocus) millisecondsToStr(time)
                else millisecondsToStr(0),
                totalTime = time,
                nextTimerMode = if (settingsState.sessionLength > 1) TimerMode.SHORT_BREAK else TimerMode.LONG_BREAK,
                nextTimeStr = millisecondsToStr(if (settingsState.sessionLength > 1) settingsState.shortBreakTime else settingsState.longBreakTime),
                currentFocusCount = 1,
                totalFocusCount = settingsState.sessionLength
            )
        }

        onCompletion()
    }

    fun undoReset() {
        lastSavedDuration = timerStateSnapshot.lastSavedDuration
        time = timerStateSnapshot.time
        cycles = timerStateSnapshot.cycles
        startTime = timerStateSnapshot.startTime
        pauseTime = timerStateSnapshot.pauseTime
        pauseDuration = timerStateSnapshot.pauseDuration
        _timerState.update { timerStateSnapshot.timerState }
    }

    /**
     * Resets the saved duration tracker. Call when the service is destroyed
     * to prevent double-counting on restart.
     */
    fun resetLastSavedDuration() {
        lastSavedDuration = 0
    }

    fun clear() {
        cycles = 0
        startTime = 0L
        pauseTime = 0L
        pauseDuration = 0L
        lastSavedDuration = 0L

        _timerState.update { currentState ->
            currentState.copy(timerRunning = false)
        }
    }
}
