package org.nsh07.pomodoro.ui.viewModel

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import org.nsh07.pomodoro.TomatoApplication
import org.nsh07.pomodoro.data.AppPreferenceRepository
import org.nsh07.pomodoro.data.TimerRepository

@OptIn(FlowPreview::class)
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

    init {
        viewModelScope.launch {
            snapshotFlow { focusTimeTextFieldState.text }
                .debounce(500)
                .collect {
                    if (it.isNotEmpty()) {
                        timerRepository.focusTime = preferenceRepository.saveIntPreference(
                            "focus_time",
                            it.toString().toInt() * 60 * 1000
                        )
                    }
                }
            snapshotFlow { shortBreakTimeTextFieldState.text }
                .debounce(500)
                .collect {
                    if (it.isNotEmpty()) {
                        timerRepository.shortBreakTime = preferenceRepository.saveIntPreference(
                            "short_break_time",
                            it.toString().toInt() * 60 * 1000
                        )
                    }
                }
            snapshotFlow { longBreakTimeTextFieldState.text }
                .debounce(500)
                .collect {
                    if (it.isNotEmpty()) {
                        timerRepository.longBreakTime = preferenceRepository.saveIntPreference(
                            "long_break_time",
                            it.toString().toInt() * 60 * 1000
                        )
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

                SettingsViewModel(
                    preferenceRepository = appPreferenceRepository,
                    timerRepository = appTimerRepository
                )
            }
        }
    }
}