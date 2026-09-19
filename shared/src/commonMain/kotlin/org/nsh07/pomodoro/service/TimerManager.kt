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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jetbrains.compose.resources.getString
import org.nsh07.pomodoro.data.StatRepository
import org.nsh07.pomodoro.data.StateRepository
import org.nsh07.pomodoro.data.Topic
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerMode
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerState
import org.nsh07.pomodoro.utils.millisecondsToStr
import tomato.shared.generated.resources.Res
import tomato.shared.generated.resources.infinite
import kotlin.time.Duration.Companion.milliseconds

class TimerManager(
    private val stateRepository: StateRepository,
    private val statRepository: StatRepository,
    private val currentTime: () -> Long,
    private val stateStore: TimerStateStore = TimerStateStore.None,
    /**
     * Platform hook that schedules a wakeup at the given value of [currentTime], or cancels the
     * pending wakeup when passed `null`. The wakeup must call [expireIntervalIfDue].
     */
    private val setExpiryAlarm: (triggerTime: Long?) -> Unit = {},
) {
    private val _timerState by lazy { stateRepository.timerState }

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
    private var hasSnapshot = false
    private val saveLock = Mutex()
    private val expiryLock = Mutex()

    private var timerJob: Job? = null

    private val restoreScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val restoreJob = restoreScope.launch {
        stateRepository.topicLoaded.first { it }
        restoreSession()
    }

    /**
     * Suspends until the stored session has been brought back. Everything that changes the timer
     * must await this first, or it would be overwritten by the session it raced.
     */
    suspend fun awaitRestore() = restoreJob.join()

    /** Writes the session out so that it outlives the process. */
    private fun persist() {
        // Until the stored session has been read back, writing would overwrite it
        if (!restoreJob.isCompleted) return

        stateStore.save(
            PersistedTimerState(
                cycles = cycles,
                startTime = startTime,
                pauseTime = pauseTime,
                pauseDuration = pauseDuration,
                lastSavedDuration = lastSavedDuration,
                timerRunning = _timerState.value.timerRunning,
                infiniteFocus = _timerState.value.infiniteFocus
            )
        )
    }

    /** Picks a killed process' session back up, instead of starting the first interval over. */
    private suspend fun restoreSession() {
        val stored = stateStore.load() ?: return
        val now = currentTime()
        // Timestamps ahead of the clock can only be left over from before a reboot
        if (stored.startTime > now || stored.pauseTime > now) return

        val topic = stateRepository.currentTopic.value
        cycles = stored.cycles.coerceIn(0, topic.sessionLength * 2 - 1)
        startTime = stored.startTime
        pauseTime = stored.pauseTime
        pauseDuration = stored.pauseDuration
        lastSavedDuration = stored.lastSavedDuration

        val infiniteLabel = getString(Res.string.infinite)
        _timerState.update {
            intervalStateOf(
                it.copy(timerRunning = stored.timerRunning, infiniteFocus = stored.infiniteFocus),
                topic,
                infiniteLabel
            )
        }

        val remaining = remainingInInterval()
        time = remaining
        _timerState.update { it.copy(timeStr = remainingStr(it, remaining)) }

        updateExpiryAlarm()
    }

    /**
     * Time actually spent in the current interval, measured with the monotonic clock provided by
     * [currentTime].
     */
    private fun elapsedInInterval(): Long {
        if (startTime == 0L) return 0L
        val now = if (pauseTime != 0L) pauseTime else currentTime()
        return (now - startTime - pauseDuration).coerceAtLeast(0L)
    }

    /** Time left in the current interval. Negative once the interval is over. */
    private fun remainingInInterval(): Long {
        val currentTopic = stateRepository.currentTopic.value
        val timerState = _timerState.value
        val elapsed = elapsedInInterval()

        return when (timerState.timerMode) {
            TimerMode.FOCUS ->
                if (!timerState.infiniteFocus) currentTopic.focusTime - elapsed
                else Long.MAX_VALUE - elapsed

            TimerMode.SHORT_BREAK -> currentTopic.shortBreakTime - elapsed

            else -> currentTopic.longBreakTime - elapsed
        }
    }

    private fun updateExpiryAlarm() {
        val timerState = _timerState.value
        val remaining = remainingInInterval()
        val infiniteFocus = timerState.infiniteFocus && timerState.timerMode == TimerMode.FOCUS

        // `remaining` is close to Long.MAX_VALUE for an infinite focus interval, which would
        // overflow when added to the current time
        setExpiryAlarm(
            if (timerState.timerRunning && !infiniteFocus) currentTime() + remaining.coerceAtLeast(0)
            else null
        )
    }

    /**
     * Marks the current interval as fresh, discarding any time accounted for so far.
     *
     * @param startNow Whether the new interval starts counting immediately
     */
    private fun beginInterval(startNow: Boolean) {
        lastSavedDuration = 0L
        pauseTime = 0L
        pauseDuration = 0L
        startTime = if (startNow) currentTime() else 0L
    }

    /**
     * Starts the current interval, or resumes it if it was paused.
     */
    private fun resumeInterval() {
        if (startTime == 0L) {
            startTime = currentTime()
            pauseTime = 0L
            pauseDuration = 0L
        } else if (pauseTime != 0L) {
            pauseDuration += (currentTime() - pauseTime).coerceAtLeast(0L)
            pauseTime = 0L
        }
    }

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
        // A session whose loop died with its service only looks like it is running, so take it
        // over instead of pausing it. Pausing would leave the button doing nothing visible.
        if (_timerState.value.timerRunning && timerJob?.isActive == true) {
            pauseTime = currentTime()
            setDoNotDisturb(false)
            onPause(time)
            _timerState.update { currentState ->
                currentState.copy(timerRunning = false)
            }
            updateExpiryAlarm()
            persist()
        } else {
            if (_timerState.value.timerMode == TimerMode.FOCUS) setDoNotDisturb(true)
            else setDoNotDisturb(false)
            onStart()
            _timerState.update { it.copy(timerRunning = true) }
            resumeInterval()
            updateExpiryAlarm()
            persist()
            startTimerLoop(scope, onTick, onTimerExpired, onSkipComplete, setDoNotDisturb)
        }

        onStateChanged()
    }

    /**
     * Starts ticking a restored session, for a process that has no timer loop of its own yet.
     * See [toggleTimer] for the callbacks.
     */
    fun startLoopIfRunning(
        scope: CoroutineScope,
        onTick: suspend (remainingTime: Long, updateNotification: Boolean, updateWidget: Boolean) -> Unit,
        onTimerExpired: suspend () -> Unit,
        onSkipComplete: suspend () -> Unit,
        setDoNotDisturb: (Boolean) -> Unit,
    ) {
        if (!_timerState.value.timerRunning || timerJob?.isActive == true) return
        if (_timerState.value.timerMode == TimerMode.FOCUS) setDoNotDisturb(true)
        startTimerLoop(scope, onTick, onTimerExpired, onSkipComplete, setDoNotDisturb)
    }

    private fun startTimerLoop(
        scope: CoroutineScope,
        onTick: suspend (remainingTime: Long, updateNotification: Boolean, updateWidget: Boolean) -> Unit,
        onTimerExpired: suspend () -> Unit,
        onSkipComplete: suspend () -> Unit,
        setDoNotDisturb: (Boolean) -> Unit,
    ) {
        var iterations = -1
        var notificationUpdateCounter = -1

        timerJob?.cancel() // never let two timer loops run at once
        timerJob = scope.launch {
            while (true) {
                if (!_timerState.value.timerRunning) break

                val elapsed = elapsedInInterval()
                time = remainingInInterval()

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
                    expireIntervalIfDue(onTimerExpired, onSkipComplete, setDoNotDisturb)
                    break
                } else {
                    _timerState.update { it.copy(timeStr = remainingStr(it, time)) }

                    if (elapsed - lastSavedDuration > SAVE_INTERVAL) saveTimeToDb()
                }

                delay((1000f / stateRepository.timerFrequency).toLong().milliseconds)
            }
        }
    }

    private fun remainingStr(state: TimerState, remaining: Long): String =
        if (!state.infiniteFocus || state.timerMode != TimerMode.FOCUS)
            millisecondsToStr(remaining.coerceAtLeast(0))
        else millisecondsToStr((state.totalTime - remaining).coerceAtLeast(0)) // elapsed time

    /**
     * Ends the current interval if its time has run out, leaving the timer paused at the start of
     * the next one.
     *
     * The timer loop and the expiry alarm both call this, so the check and the advance happen
     * together under [expiryLock]. A caller that arrives second, or with a stale alarm, finds
     * nothing left to do.
     *
     * @return whether this call was the one that ended the interval
     */
    suspend fun expireIntervalIfDue(
        onTimerExpired: suspend () -> Unit,
        onSkipComplete: suspend () -> Unit,
        setDoNotDisturb: (Boolean) -> Unit,
    ): Boolean = expiryLock.withLock {
        if (!_timerState.value.timerRunning || remainingInInterval() > 0) return@withLock false

        // The next interval must not start counting until the timer is started again
        advanceTimer(
            onStart = onTimerExpired,
            onCompletion = onSkipComplete,
            setDoNotDisturb = setDoNotDisturb,
            startNextInterval = false
        )
        _timerState.update { currentState ->
            currentState.copy(timerRunning = false)
        }
        updateExpiryAlarm()
        persist()

        true
    }

    /**
     * Writes the part of the current interval that has elapsed since the last save to the database.
     */
    suspend fun saveTimeToDb() {
        saveLock.withLock { saveElapsedTime() }
        persist()
    }

    /**
     * See [saveTimeToDb]. Must only be called while holding [saveLock].
     */
    private suspend fun saveElapsedTime() {
        val timerState = _timerState.value
        val currentTopic = stateRepository.currentTopic.value

        // An interval cannot contribute more time than its own length
        val intervalDuration = when (timerState.timerMode) {
            TimerMode.FOCUS ->
                if (!timerState.infiniteFocus) currentTopic.focusTime else Long.MAX_VALUE

            TimerMode.SHORT_BREAK -> currentTopic.shortBreakTime

            else -> currentTopic.longBreakTime
        }

        val elapsed = elapsedInInterval().coerceAtMost(intervalDuration)
        val duration = (elapsed - lastSavedDuration).coerceIn(0L, MAX_SAVED_DURATION)

        if (duration > 0L) {
            when (timerState.timerMode) {
                TimerMode.FOCUS -> statRepository.addFocusTime(currentTopic.id, duration)

                else -> statRepository.addBreakTime(currentTopic.id, duration)
            }
        }

        lastSavedDuration = elapsed
    }

    /**
     * Ends the current interval and moves the timer on to the next one.
     */
    suspend fun skipTimer(
        onStart: suspend () -> Unit,
        onCompletion: suspend () -> Unit,
        setDoNotDisturb: (Boolean) -> Unit
    ) {
        advanceTimer(
            onStart = onStart,
            onCompletion = onCompletion,
            setDoNotDisturb = setDoNotDisturb,
            startNextInterval = _timerState.value.timerRunning
        )
        updateExpiryAlarm()
    }

    /**
     * See [skipTimer].
     *
     * @param startNextInterval Whether the next interval starts counting immediately, see
     * [beginInterval]
     */
    private suspend fun advanceTimer(
        onStart: suspend () -> Unit,
        onCompletion: suspend () -> Unit,
        setDoNotDisturb: (Boolean) -> Unit,
        startNextInterval: Boolean
    ) {
        val currentTopic = stateRepository.currentTopic.value

        // Flushing and resetting must be atomic, else a save could record the interval twice
        saveLock.withLock {
            saveElapsedTime()
            beginInterval(startNow = startNextInterval)
        }

        onStart()

        cycles = (cycles + 1) % (currentTopic.sessionLength * 2)

        val infiniteLabel = getString(Res.string.infinite)

        if (_timerState.value.timerRunning) setDoNotDisturb(cycles % 2 == 0)

        _timerState.update { intervalStateOf(it, currentTopic, infiniteLabel) }
        time = _timerState.value.totalTime

        persist()

        onCompletion()
    }

    /**
     * [state] rewritten to describe the interval [cycles] points at.
     *
     * @param infiniteLabel [Res.string.infinite], resolved by the caller since this cannot suspend
     */
    private fun intervalStateOf(
        state: TimerState,
        topic: Topic,
        infiniteLabel: String
    ): TimerState {
        val infiniteFocus = state.infiniteFocus

        return if (cycles % 2 == 0) {
            val newTime = if (!infiniteFocus) topic.focusTime else Long.MAX_VALUE
            val long = cycles == (topic.sessionLength - 1) * 2

            state.copy(
                timerMode = TimerMode.FOCUS,
                timeStr = if (!infiniteFocus) millisecondsToStr(newTime)
                else millisecondsToStr(0),
                totalTime = newTime,
                nextTimerMode = if (long) TimerMode.LONG_BREAK else TimerMode.SHORT_BREAK,
                nextTimeStr = if (long) millisecondsToStr(topic.longBreakTime)
                else millisecondsToStr(topic.shortBreakTime),
                currentFocusCount = cycles / 2 + 1,
                totalFocusCount = topic.sessionLength
            )
        } else {
            val long = cycles == (topic.sessionLength * 2) - 1
            val newTime = if (long) topic.longBreakTime else topic.shortBreakTime

            state.copy(
                timerMode = if (long) TimerMode.LONG_BREAK else TimerMode.SHORT_BREAK,
                timeStr = millisecondsToStr(newTime),
                totalTime = newTime,
                nextTimerMode = TimerMode.FOCUS,
                nextTimeStr = if (!infiniteFocus) millisecondsToStr(topic.focusTime)
                else infiniteLabel
            )
        }
    }

    /**
     * Stops the timer and puts it back at the start of the first focus interval.
     */
    suspend fun resetTimer(onCompletion: () -> Unit) {
        val currentTopic = stateRepository.currentTopic.value

        timerJob?.cancel()
        if (_timerState.value.timerRunning) {
            pauseTime = currentTime()
            _timerState.update { it.copy(timerRunning = false) }
        }

        saveLock.withLock {
            saveElapsedTime()
            // Snapshotted after flushing, so that an undo cannot save the same time twice
            timerStateSnapshot.save(
                lastSavedDuration,
                time,
                cycles,
                startTime,
                pauseTime,
                pauseDuration,
                _timerState.value
            )
            hasSnapshot = true
            beginInterval(startNow = false)
        }

        cycles = 0

        val infiniteFocus = _timerState.value.infiniteFocus
        val newTime = if (!infiniteFocus) currentTopic.focusTime else Long.MAX_VALUE
        time = newTime

        _timerState.update { currentState ->
            currentState.copy(
                timerMode = TimerMode.FOCUS,
                timeStr = if (!infiniteFocus) millisecondsToStr(newTime)
                else millisecondsToStr(0),
                totalTime = newTime,
                nextTimerMode = if (currentTopic.sessionLength > 1) TimerMode.SHORT_BREAK else TimerMode.LONG_BREAK,
                nextTimeStr = millisecondsToStr(if (currentTopic.sessionLength > 1) currentTopic.shortBreakTime else currentTopic.longBreakTime),
                currentFocusCount = 1,
                totalFocusCount = currentTopic.sessionLength
            )
        }

        updateExpiryAlarm()
        persist()

        onCompletion()
    }

    fun undoReset() {
        if (!hasSnapshot) return // nothing to restore, the timer was never reset
        lastSavedDuration = timerStateSnapshot.lastSavedDuration
        time = timerStateSnapshot.time
        cycles = timerStateSnapshot.cycles
        startTime = timerStateSnapshot.startTime
        pauseTime = timerStateSnapshot.pauseTime
        pauseDuration = timerStateSnapshot.pauseDuration
        _timerState.update { timerStateSnapshot.timerState }
        updateExpiryAlarm()
        persist()
    }

    private companion object {
        const val SAVE_INTERVAL = 60000L
        const val MAX_SAVED_DURATION = 24 * 60 * 60 * 1000L
    }
}
