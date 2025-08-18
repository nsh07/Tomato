/*
 * Copyright (c) 2025 Nishant Mishra
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.ui.timerScreen.viewModel

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
import org.nsh07.pomodoro.data.PreferenceRepository
import org.nsh07.pomodoro.data.Stat
import org.nsh07.pomodoro.data.StatRepository
import org.nsh07.pomodoro.data.TimerRepository
import org.nsh07.pomodoro.utils.millisecondsToStr
import java.time.LocalDate

@OptIn(FlowPreview::class)
class TimerViewModel(
    private val preferenceRepository: PreferenceRepository,
    private val statRepository: StatRepository,
    private val timerRepository: TimerRepository
) : ViewModel() {
    // TODO: Document code
    private val _timerState = MutableStateFlow(
        TimerState(
            totalTime = timerRepository.focusTime,
            timeStr = millisecondsToStr(timerRepository.focusTime),
            nextTimeStr = millisecondsToStr(timerRepository.shortBreakTime)
        )
    )

    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()
    var timerJob: Job? = null
    private val _time = MutableStateFlow(timerRepository.focusTime)

    val time: StateFlow<Long> = _time.asStateFlow()
    private var cycles = 0

    private var startTime = 0L
    private var pauseTime = 0L
    private var pauseDuration = 0L

    init {
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
            timerRepository.sessionLength = preferenceRepository.getIntPreference("session_length")
                ?: preferenceRepository.saveIntPreference(
                    "session_length",
                    timerRepository.sessionLength
                )

            resetTimer()

            var lastDate = statRepository.getLastDate()
            val today = LocalDate.now()

            // Fills dates between today and lastDate with 0s to ensure continuous history
            while ((lastDate?.until(today)?.days ?: -1) > 0) {
                lastDate = lastDate?.plusDays(1)
                statRepository.insertStat(Stat(lastDate!!, 0, 0, 0, 0, 0))
            }

            delay(1500)

            _timerState.update { currentState ->
                currentState.copy(showBrandTitle = false)
            }
        }
    }

    fun onAction(action: TimerAction) {
        when (action) {
            TimerAction.ResetTimer -> resetTimer()
            TimerAction.SkipTimer -> skipTimer()
            TimerAction.ToggleTimer -> toggleTimer()
        }
    }

    private fun resetTimer() {
        viewModelScope.launch {
            saveTimeToDb()
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
                    nextTimeStr = millisecondsToStr(if (timerRepository.sessionLength > 1) timerRepository.shortBreakTime else timerRepository.longBreakTime)
                )
            }
        }
    }

    private fun skipTimer() {
        viewModelScope.launch {
            saveTimeToDb()
            startTime = 0L
            pauseTime = 0L
            pauseDuration = 0L

            cycles = (cycles + 1) % (timerRepository.sessionLength * 2)

            if (cycles % 2 == 0) {
                _time.update { timerRepository.focusTime }
                _timerState.update { currentState ->
                    currentState.copy(
                        timerMode = TimerMode.FOCUS,
                        timeStr = millisecondsToStr(time.value),
                        totalTime = time.value,
                        nextTimerMode = if (cycles == (timerRepository.sessionLength - 1) * 2) TimerMode.LONG_BREAK else TimerMode.SHORT_BREAK,
                        nextTimeStr = if (cycles == (timerRepository.sessionLength - 1) * 2) millisecondsToStr(
                            timerRepository.longBreakTime
                        ) else millisecondsToStr(
                            timerRepository.shortBreakTime
                        )
                    )
                }
            } else {
                val long = cycles == (timerRepository.sessionLength * 2) - 1
                _time.update { if (long) timerRepository.longBreakTime else timerRepository.shortBreakTime }

                _timerState.update { currentState ->
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
    }

    private fun toggleTimer() {
        if (timerState.value.timerRunning) {
            _timerState.update { currentState ->
                currentState.copy(timerRunning = false)
            }
            timerJob?.cancel()
            pauseTime = SystemClock.elapsedRealtime()
        } else {
            _timerState.update { it.copy(timerRunning = true) }
            if (pauseTime != 0L) pauseDuration += SystemClock.elapsedRealtime() - pauseTime

            timerJob = viewModelScope.launch {
                while (true) {
                    if (!timerState.value.timerRunning) break
                    if (startTime == 0L) startTime = SystemClock.elapsedRealtime()

                    _time.update {
                        when (timerState.value.timerMode) {
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

                        _timerState.update { currentState ->
                            currentState.copy(timerRunning = false)
                        }
                        timerJob?.cancel()
                    } else {
                        _timerState.update { currentState ->
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

    suspend fun saveTimeToDb() {
        when (timerState.value.timerMode) {
            TimerMode.FOCUS -> statRepository
                .addFocusTime((timerState.value.totalTime - time.value).coerceAtLeast(0L))

            else -> statRepository
                .addBreakTime((timerState.value.totalTime - time.value).coerceAtLeast(0L))
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as TomatoApplication)
                val appPreferenceRepository = application.container.appPreferenceRepository
                val appStatRepository = application.container.appStatRepository
                val appTimerRepository = application.container.appTimerRepository

                TimerViewModel(
                    preferenceRepository = appPreferenceRepository,
                    statRepository = appStatRepository,
                    timerRepository = appTimerRepository
                )
            }
        }
    }
}