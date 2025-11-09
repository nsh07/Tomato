/*
 * Copyright (c) 2025 Nishant Mishra
 *
 * This file is part of Tomato - a minimalist pomodoro timer for Android.
 *
 * Tomato is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Tomato is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Tomato.
 * If not, see <https://www.gnu.org/licenses/>.
 */

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
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsViewModel
import org.nsh07.pomodoro.ui.theme.TomatoTheme
import org.nsh07.pomodoro.utils.toColor

class MainActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels(factoryProducer = { SettingsViewModel.Factory })

    private val appContainer by lazy {
        (application as TomatoApplication).container
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        appContainer.activityTurnScreenOn = {
            setShowWhenLocked(it)
            setTurnScreenOn(it)
        }

        setContent {
            val settingsState by settingsViewModel.settingsState.collectAsStateWithLifecycle()

            val darkTheme = when (settingsState.theme) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            val seed = settingsState.colorScheme.toColor()

            val isPlus by settingsViewModel.isPlus.collectAsStateWithLifecycle()

            TomatoTheme(
                darkTheme = darkTheme,
                seedColor = seed,
                blackTheme = settingsState.blackTheme
            ) {
                val colorScheme = colorScheme
                LaunchedEffect(colorScheme) {
                    appContainer.appTimerRepository.colorScheme = colorScheme
                }

                AppScreen(
                    isPlus = isPlus,
                    isAODEnabled = settingsState.aodEnabled,
                    setTimerFrequency = {
                        appContainer.appTimerRepository.timerFrequency = it
                    }
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
        appContainer.appTimerRepository.timerFrequency = 60f
    }
}