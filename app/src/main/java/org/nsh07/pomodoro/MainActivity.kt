package org.nsh07.pomodoro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.LaunchedEffect
import org.nsh07.pomodoro.ui.AppScreen
import org.nsh07.pomodoro.ui.NavItem
import org.nsh07.pomodoro.ui.Screen
import org.nsh07.pomodoro.ui.statsScreen.viewModel.StatsViewModel
import org.nsh07.pomodoro.ui.theme.TomatoTheme
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerViewModel

class MainActivity : ComponentActivity() {

    private val timerViewModel: TimerViewModel by viewModels(factoryProducer = { TimerViewModel.Factory })
    private val statsViewModel: StatsViewModel by viewModels(factoryProducer = { StatsViewModel.Factory })

    private val appContainer by lazy {
        (application as TomatoApplication).container
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TomatoTheme {
                val colorScheme = colorScheme
                LaunchedEffect(colorScheme) {
                    appContainer.appTimerRepository.colorScheme = colorScheme
                }

                timerViewModel.setCompositionLocals(colorScheme)
                AppScreen(timerViewModel = timerViewModel, statsViewModel = statsViewModel)
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
                R.drawable.hourglass,
                R.drawable.hourglass_filled,
                "Timer"
            ),
            NavItem(
                Screen.Stats,
                R.drawable.monitoring,
                R.drawable.monitoring_filled,
                "Stats"
            ),
            NavItem(
                Screen.Settings,
                R.drawable.settings,
                R.drawable.settings_filled,
                "Settings"
            )
        )
    }
}