package org.nsh07.pomodoro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import org.nsh07.pomodoro.ui.AppScreen
import org.nsh07.pomodoro.ui.NavItem
import org.nsh07.pomodoro.ui.Screen
import org.nsh07.pomodoro.ui.theme.TomatoTheme
import org.nsh07.pomodoro.ui.viewModel.UiViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: UiViewModel by viewModels(factoryProducer = { UiViewModel.Factory })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TomatoTheme {
                AppScreen(viewModel = viewModel)
            }
        }
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