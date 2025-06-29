package org.nsh07.pomodoro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import org.nsh07.pomodoro.ui.AppScreen
import org.nsh07.pomodoro.ui.theme.PomodoroTheme
import org.nsh07.pomodoro.ui.viewModel.UiViewModel

class MainActivity : ComponentActivity() {
    val viewModel: UiViewModel by viewModels<UiViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PomodoroTheme {
                AppScreen(viewModel)
            }
        }
    }
}