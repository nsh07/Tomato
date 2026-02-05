package org.nsh07.pomodoro.di

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.MutableStateFlow
import org.nsh07.pomodoro.data.StateRepository

class TimerStateHolder(private val stateRepository: StateRepository) {
    val time: MutableStateFlow<Long> by lazy {
        MutableStateFlow(stateRepository.settingsState.value.focusTime)
    }
}

class ActivityCallbacks {
    var activityTurnScreenOn: (Boolean) -> Unit = {}
}

data class FlavorUI(
    val tomatoPlusPaywallDialog: @Composable (Boolean, () -> Unit) -> Unit,
    val topButton: @Composable (Modifier) -> Unit,
    val bottomButton: @Composable (Modifier) -> Unit
)

data class AppInfo(
    val debug: Boolean,
    val versionName: String,
    val versionCode: Long
)
