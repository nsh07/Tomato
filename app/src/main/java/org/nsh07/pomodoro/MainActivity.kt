package org.nsh07.pomodoro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.nsh07.pomodoro.ui.AppScreen
import org.nsh07.pomodoro.ui.NavItem
import org.nsh07.pomodoro.ui.Screen
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsViewModel
import org.nsh07.pomodoro.ui.theme.TomatoTheme
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerViewModel
import org.nsh07.pomodoro.utils.toColor

class MainActivity : ComponentActivity() {

        private val timerViewModel: TimerViewModel by viewModels(factoryProducer = { TimerViewModel.Factory })
    private val settingsViewModel: SettingsViewModel by viewModels(factoryProducer = { SettingsViewModel.Factory })

    private val appContainer by lazy {
        (application as TomatoApplication).container
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val preferencesState by settingsViewModel.preferencesState.collectAsStateWithLifecycle()

            val darkTheme = when (preferencesState.theme) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            val seed = preferencesState.colorScheme.toColor()

            TomatoTheme(
                darkTheme = darkTheme,
                seedColor = seed,
                blackTheme = preferencesState.blackTheme
            ) {
                val colorScheme = colorScheme
                LaunchedEffect(colorScheme) {
                    appContainer.appTimerRepository.colorScheme = colorScheme
                }

                AppScreen(
                    timerViewModel = timerViewModel,
                    isAODEnabled = preferencesState.aodEnabled
                )
            }
        }
    }


    override fun onStop() {
        super.onStop()
        // Reduce the timer loop frequency when not visible to save battery power
        appContainer.appTimerRepository.timerFrequency = 1f
    }

    override fun onStart() {
        super.onStart()
        // Increase the timer loop frequency again when visible to make the progress smoother
        appContainer.appTimerRepository.timerFrequency = 10f
    }

    companion object {
        val screens = listOf(
            NavItem(
                Screen.Timer,
                R.drawable.timer_outlined,
                R.drawable.timer_filled,
                R.string.timer
            ),
            NavItem(
                Screen.Stats,
                R.drawable.monitoring,
                R.drawable.monitoring_filled,
                R.string.stats
            ),
            NavItem(
                Screen.Settings,
                R.drawable.settings,
                R.drawable.settings_filled,
                R.string.settings
            )
        )
    }
}