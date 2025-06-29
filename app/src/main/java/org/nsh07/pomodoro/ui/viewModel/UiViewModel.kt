package org.nsh07.pomodoro.ui.viewModel

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

class UiViewModel : ViewModel() {
    val focusTime = 10
    val shortBreakTime = 5
    val longBreakTime = 20

    private val _uiState = MutableStateFlow(
        UiState(
            totalTime = focusTime,
            remainingTime = focusTime,
            timeStr = secondsToStr(focusTime),
            nextTimeStr = secondsToStr(shortBreakTime)
        )
    )
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    var timerJob: Job? = null

    var time = focusTime
    var cycles = 0

    fun resetTimer() {
        time = focusTime
        cycles = 0

        _uiState.update { currentState ->
            currentState.copy(
                timerMode = TimerMode.FOCUS,
                timeStr = secondsToStr(time),
                totalTime = time,
                remainingTime = time,
                nextTimerMode = TimerMode.SHORT_BREAK,
                nextTimeStr = secondsToStr(shortBreakTime)
            )
        }
    }

    fun toggleTimer() {
        if (uiState.value.timerRunning) {
            _uiState.update { currentState ->
                currentState.copy(timerRunning = false)
            }
            timerJob?.cancel()
        } else {
            _uiState.update { it.copy(timerRunning = true) }
            timerJob = viewModelScope.launch {
                while (true) {
                    if (!uiState.value.timerRunning) break
                    time--;

                    if (time < 0) {
                        cycles++;

                        if (cycles % 2 == 0) {
                            time = focusTime
                            _uiState.update { currentState ->
                                currentState.copy(
                                    timerMode = TimerMode.FOCUS,
                                    timeStr = secondsToStr(time),
                                    totalTime = time,
                                    remainingTime = time,
                                    nextTimerMode = if (cycles % 6 == 0) TimerMode.LONG_BREAK else TimerMode.SHORT_BREAK,
                                    nextTimeStr = if (cycles % 6 == 0) secondsToStr(longBreakTime) else secondsToStr(
                                        shortBreakTime
                                    )
                                )
                            }
                        } else {
                            val long = cycles % 7 == 0
                            time = if (long) longBreakTime else shortBreakTime

                            _uiState.update { currentState ->
                                currentState.copy(
                                    timerMode = if (long) TimerMode.LONG_BREAK else TimerMode.SHORT_BREAK,
                                    timeStr = secondsToStr(time),
                                    totalTime = time,
                                    remainingTime = time,
                                    nextTimerMode = TimerMode.FOCUS,
                                    nextTimeStr = secondsToStr(focusTime)
                                )
                            }
                        }
                    } else {
                        _uiState.update { currentState ->
                            currentState.copy(
                                timeStr = secondsToStr(time),
                                remainingTime = time
                            )
                        }
                    }

                    delay(1000)
                }
            }
        }
    }

    private fun secondsToStr(t: Int): String {
        val min = t / 60
        val sec = t % 60
        return String.format(locale = Locale.getDefault(), "%02d:%02d", min, sec)
    }
}