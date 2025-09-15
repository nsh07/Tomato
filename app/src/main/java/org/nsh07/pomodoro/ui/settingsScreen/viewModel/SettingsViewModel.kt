/*
 * Copyright (c) 2025 Nishant Mishra
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.ui.settingsScreen.viewModel

import android.net.Uri
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SliderState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.nsh07.pomodoro.TomatoApplication
import org.nsh07.pomodoro.data.AppPreferenceRepository
import org.nsh07.pomodoro.data.TimerRepository

@OptIn(FlowPreview::class, ExperimentalMaterial3Api::class)
class SettingsViewModel(
    private val preferenceRepository: AppPreferenceRepository,
    private val timerRepository: TimerRepository
) : ViewModel() {
    val focusTimeTextFieldState =
        TextFieldState((timerRepository.focusTime / 60000).toString())
    val shortBreakTimeTextFieldState =
        TextFieldState((timerRepository.shortBreakTime / 60000).toString())
    val longBreakTimeTextFieldState =
        TextFieldState((timerRepository.longBreakTime / 60000).toString())

    val sessionsSliderState = SliderState(
        value = timerRepository.sessionLength.toFloat(),
        steps = 4,
        valueRange = 1f..6f,
        onValueChangeFinished = ::updateSessionLength
    )

    val alarmSound =
        preferenceRepository.getStringPreferenceFlow("alarm_sound").distinctUntilChanged()
    val alarmEnabled =
        preferenceRepository.getBooleanPreferenceFlow("alarm_enabled").distinctUntilChanged()
    val vibrateEnabled =
        preferenceRepository.getBooleanPreferenceFlow("vibrate_enabled").distinctUntilChanged()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            snapshotFlow { focusTimeTextFieldState.text }
                .debounce(500)
                .collect {
                    if (it.isNotEmpty()) {
                        timerRepository.focusTime = preferenceRepository.saveIntPreference(
                            "focus_time",
                            it.toString().toInt() * 60 * 1000
                        ).toLong()
                    }
                }
        }
        viewModelScope.launch(Dispatchers.IO) {
            snapshotFlow { shortBreakTimeTextFieldState.text }
                .debounce(500)
                .collect {
                    if (it.isNotEmpty()) {
                        timerRepository.shortBreakTime = preferenceRepository.saveIntPreference(
                            "short_break_time",
                            it.toString().toInt() * 60 * 1000
                        ).toLong()
                    }
                }
        }
        viewModelScope.launch(Dispatchers.IO) {
            snapshotFlow { longBreakTimeTextFieldState.text }
                .debounce(500)
                .collect {
                    if (it.isNotEmpty()) {
                        timerRepository.longBreakTime = preferenceRepository.saveIntPreference(
                            "long_break_time",
                            it.toString().toInt() * 60 * 1000
                        ).toLong()
                    }
                }
        }
    }

    private fun updateSessionLength() {
        viewModelScope.launch {
            timerRepository.sessionLength = preferenceRepository.saveIntPreference(
                "session_length",
                sessionsSliderState.value.toInt()
            )
        }
    }

    fun saveAlarmEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferenceRepository.saveBooleanPreference("alarm_enabled", enabled)
            timerRepository.alarmEnabled = enabled
        }
    }

    fun saveVibrateEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferenceRepository.saveBooleanPreference("vibrate_enabled", enabled)
            timerRepository.vibrateEnabled = enabled
        }
    }

    fun saveAlarmSound(uri: Uri?) {
        viewModelScope.launch {
            preferenceRepository.saveStringPreference("alarm_sound", uri.toString())
            timerRepository.alarmSoundUri = uri
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as TomatoApplication)
                val appPreferenceRepository = application.container.appPreferenceRepository
                val appTimerRepository = application.container.appTimerRepository

                SettingsViewModel(
                    preferenceRepository = appPreferenceRepository,
                    timerRepository = appTimerRepository
                )
            }
        }
    }
}