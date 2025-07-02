package org.nsh07.pomodoro.ui.viewModel

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.ceil

class UiViewModel : ViewModel() {
    val focusTime = 25 * 60 * 1000
    val shortBreakTime = 5 * 60 * 1000
    val longBreakTime = 15 * 60 * 1000

    private val _uiState = MutableStateFlow(
        UiState(
            totalTime = focusTime,
            timeStr = millisecondsToStr(focusTime),
            nextTimeStr = millisecondsToStr(shortBreakTime)
        )
    )
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    var timerJob: Job? = null

    private val _time = MutableStateFlow(focusTime)
    val time: StateFlow<Int> = _time.asStateFlow()

    private var cycles = 0
    private var startTime = 0L
    private var pauseTime = 0L
    private var pauseDuration = 0L

    fun resetTimer() {
        _time.update { focusTime }
        cycles = 0
        startTime = 0L
        pauseTime = 0L
        pauseDuration = 0L

        _uiState.update { currentState ->
            currentState.copy(
                timerMode = TimerMode.FOCUS,
                timeStr = millisecondsToStr(time.value),
                totalTime = time.value,
                nextTimerMode = TimerMode.SHORT_BREAK,
                nextTimeStr = millisecondsToStr(shortBreakTime)
            )
        }
    }

    fun skipTimer() {
        startTime = 0L
        pauseTime = 0L
        pauseDuration = 0L
        cycles = (cycles + 1) % 8

        if (cycles % 2 == 0) {
            _time.update { focusTime }
            _uiState.update { currentState ->
                currentState.copy(
                    timerMode = TimerMode.FOCUS,
                    timeStr = millisecondsToStr(time.value),
                    totalTime = time.value,
                    nextTimerMode = if (cycles == 6) TimerMode.LONG_BREAK else TimerMode.SHORT_BREAK,
                    nextTimeStr = if (cycles == 6) millisecondsToStr(
                        longBreakTime
                    ) else millisecondsToStr(
                        shortBreakTime
                    )
                )
            }
        } else {
            val long = cycles == 7
            _time.update { if (long) longBreakTime else shortBreakTime }

            _uiState.update { currentState ->
                currentState.copy(
                    timerMode = if (long) TimerMode.LONG_BREAK else TimerMode.SHORT_BREAK,
                    timeStr = millisecondsToStr(time.value),
                    totalTime = time.value,
                    nextTimerMode = TimerMode.FOCUS,
                    nextTimeStr = millisecondsToStr(focusTime)
                )
            }
        }
    }

    fun toggleTimer() {
        if (uiState.value.timerRunning) {
            _uiState.update { currentState ->
                currentState.copy(timerRunning = false)
            }
            timerJob?.cancel()
            pauseTime = SystemClock.elapsedRealtime()
        } else {
            _uiState.update { it.copy(timerRunning = true) }
            if (pauseTime != 0L) pauseDuration += SystemClock.elapsedRealtime() - pauseTime

            timerJob = viewModelScope.launch {
                while (true) {
                    if (!uiState.value.timerRunning) break
                    if (startTime == 0L) startTime = SystemClock.elapsedRealtime()

                    _time.update {
                        when (uiState.value.timerMode) {
                            TimerMode.FOCUS ->
                                focusTime - (SystemClock.elapsedRealtime() - startTime - pauseDuration).toInt()
                            TimerMode.SHORT_BREAK ->
                                shortBreakTime - (SystemClock.elapsedRealtime() - startTime - pauseDuration).toInt()
                            else ->
                                longBreakTime - (SystemClock.elapsedRealtime() - startTime - pauseDuration).toInt()
                        }
                    }

                    if (time.value < 0) {
                        startTime = 0L
                        pauseTime = 0L
                        pauseDuration = 0L
                        cycles = (cycles + 1) % 8

                        if (cycles % 2 == 0) {
                            _time.update { focusTime }
                            _uiState.update { currentState ->
                                currentState.copy(
                                    timerMode = TimerMode.FOCUS,
                                    timeStr = millisecondsToStr(time.value),
                                    totalTime = time.value,
                                    nextTimerMode = if (cycles == 6) TimerMode.LONG_BREAK else TimerMode.SHORT_BREAK,
                                    nextTimeStr = if (cycles == 6) millisecondsToStr(
                                        longBreakTime
                                    ) else millisecondsToStr(
                                        shortBreakTime
                                    )
                                )
                            }
                        } else {
                            val long = cycles == 7
                            _time.update { if (long) longBreakTime else shortBreakTime }

                            _uiState.update { currentState ->
                                currentState.copy(
                                    timerMode = if (long) TimerMode.LONG_BREAK else TimerMode.SHORT_BREAK,
                                    timeStr = millisecondsToStr(time.value),
                                    totalTime = time.value,
                                    nextTimerMode = TimerMode.FOCUS,
                                    nextTimeStr = millisecondsToStr(focusTime)
                                )
                            }
                        }
                    } else {
                        _uiState.update { currentState ->
                            currentState.copy(
                                timeStr = millisecondsToStr(time.value)
                            )
                        }
                    }

                    delay(100)
                }
            }
        }
    }

    private fun millisecondsToStr(t: Int): String {
        val min = (ceil(t / 1000.0).toInt() / 60)
        val sec = (ceil(t / 1000.0).toInt() % 60)
        return String.format(locale = Locale.getDefault(), "%02d:%02d", min, sec)
    }
}