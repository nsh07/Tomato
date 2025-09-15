/*
 * Copyright (c) 2025 Nishant Mishra
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.ui.timerScreen.viewModel

import android.app.Application
import androidx.compose.material3.ColorScheme
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
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
    application: Application,
    private val preferenceRepository: PreferenceRepository,
    private val statRepository: StatRepository,
    private val timerRepository: TimerRepository,
    private val _timerState: MutableStateFlow<TimerState>,
    private val _time: MutableStateFlow<Long>
) : AndroidViewModel(application) {
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    val time: StateFlow<Long> = _time.asStateFlow()
    private var cycles = 0

    private var startTime = 0L
    private var pauseTime = 0L
    private var pauseDuration = 0L

    private lateinit var cs: ColorScheme

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

            timerRepository.alarmEnabled = (
                    preferenceRepository.getIntPreference("alarm_enabled")
                        ?: preferenceRepository.saveIntPreference(
                            "alarm_enabled",
                            1
                        )
                    ) == 1
            timerRepository.vibrateEnabled = (
                    preferenceRepository.getIntPreference("vibrate_enabled")
                        ?: preferenceRepository.saveIntPreference(
                            "vibrate_enabled",
                            1
                        )
                    ) == 1

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

    fun setCompositionLocals(colorScheme: ColorScheme) {
        cs = colorScheme
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
                    nextTimeStr = millisecondsToStr(if (timerRepository.sessionLength > 1) timerRepository.shortBreakTime else timerRepository.longBreakTime),
                    currentFocusCount = 1,
                    totalFocusCount = timerRepository.sessionLength
                )
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
                val timerState = application.container.timerState
                val time = application.container.time

                TimerViewModel(
                    application = application,
                    preferenceRepository = appPreferenceRepository,
                    statRepository = appStatRepository,
                    timerRepository = appTimerRepository,
                    _timerState = timerState,
                    _time = time
                )
            }
        }
    }
}