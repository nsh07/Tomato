package org.nsh07.pomodoro.ui.viewModel

data class UiState(
    val timerMode: TimerMode = TimerMode.FOCUS,
    val timeStr: String = "25:00",
    val totalTime: Int = 25 * 60,
    val timerRunning: Boolean = false,
    val nextTimerMode: TimerMode = TimerMode.SHORT_BREAK,
    val nextTimeStr: String = "5:00"
)

enum class TimerMode {
    FOCUS, SHORT_BREAK, LONG_BREAK
}