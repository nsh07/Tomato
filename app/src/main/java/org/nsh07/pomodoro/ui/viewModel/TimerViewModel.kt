package org.nsh07.pomodoro.ui.viewModel

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nsh07.pomodoro.TomatoApplication
import org.nsh07.pomodoro.data.AppPreferenceRepository
import org.nsh07.pomodoro.data.TimerRepository
import org.nsh07.pomodoro.utils.millisecondsToStr

@OptIn(FlowPreview::class)
class TimerViewModel(
    private val preferenceRepository: AppPreferenceRepository,
    private val timerRepository: TimerRepository
) : ViewModel() {
    init {
        viewModelScope.launch(Dispatchers.IO) {
            timerRepository.focusTime = preferenceRepository.getIntPreference("focus_time")
                ?: preferenceRepository.saveIntPreference("focus_time", timerRepository.focusTime)
            timerRepository.shortBreakTime = preferenceRepository.getIntPreference("short_break_time")
                ?: preferenceRepository.saveIntPreference("short_break_time", timerRepository.shortBreakTime)
            timerRepository.longBreakTime = preferenceRepository.getIntPreference("long_break_time")
                ?: preferenceRepository.saveIntPreference("long_break_time", timerRepository.longBreakTime)

            resetTimer()
        }
    }

    private val _uiState = MutableStateFlow(
        UiState(
            totalTime = timerRepository.focusTime,
            timeStr = millisecondsToStr(timerRepository.focusTime),
            nextTimeStr = millisecondsToStr(timerRepository.shortBreakTime)
        )
    )
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    var timerJob: Job? = null

    private val _time = MutableStateFlow(timerRepository.focusTime)
    val time: StateFlow<Int> = _time.asStateFlow()

    private var cycles = 0
    private var startTime = 0L
    private var pauseTime = 0L
    private var pauseDuration = 0L

    fun resetTimer() {
        _time.update { timerRepository.focusTime }
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
                nextTimeStr = millisecondsToStr(timerRepository.shortBreakTime)
            )
        }
    }

    fun skipTimer() {
        startTime = 0L
        pauseTime = 0L
        pauseDuration = 0L
        cycles = (cycles + 1) % 8

        if (cycles % 2 == 0) {
            _time.update { timerRepository.focusTime }
            _uiState.update { currentState ->
                currentState.copy(
                    timerMode = TimerMode.FOCUS,
                    timeStr = millisecondsToStr(time.value),
                    totalTime = time.value,
                    nextTimerMode = if (cycles == 6) TimerMode.LONG_BREAK else TimerMode.SHORT_BREAK,
                    nextTimeStr = if (cycles == 6) millisecondsToStr(
                        timerRepository.longBreakTime
                    ) else millisecondsToStr(
                        timerRepository.shortBreakTime
                    )
                )
            }
        } else {
            val long = cycles == 7
            _time.update { if (long) timerRepository.longBreakTime else timerRepository.shortBreakTime }

            _uiState.update { currentState ->
                currentState.copy(
                    timerMode = if (long) TimerMode.LONG_BREAK else TimerMode.SHORT_BREAK,
                    timeStr = millisecondsToStr(time.value),
                    totalTime = time.value,
                    nextTimerMode = TimerMode.FOCUS,
                    nextTimeStr = millisecondsToStr(timerRepository.focusTime)
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
                                timerRepository.focusTime - (SystemClock.elapsedRealtime() - startTime - pauseDuration).toInt()

                            TimerMode.SHORT_BREAK ->
                                timerRepository.shortBreakTime - (SystemClock.elapsedRealtime() - startTime - pauseDuration).toInt()

                            else ->
                                timerRepository.longBreakTime - (SystemClock.elapsedRealtime() - startTime - pauseDuration).toInt()
                        }
                    }

                    if (time.value < 0) {
                        skipTimer()

                        _uiState.update { currentState ->
                            currentState.copy(timerRunning = false)
                        }
                        timerJob?.cancel()
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

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as TomatoApplication)
                val appPreferenceRepository = application.container.appPreferencesRepository
                val appTimerRepository = application.container.appTimerRepository

                TimerViewModel(
                    preferenceRepository = appPreferenceRepository,
                    timerRepository = appTimerRepository
                )
            }
        }
    }
}