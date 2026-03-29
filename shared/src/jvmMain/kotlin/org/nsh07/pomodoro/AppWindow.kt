/*
 * Copyright (c) 2026 Nishant Mishra
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

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import org.koin.compose.koinInject
import org.nsh07.pomodoro.data.StateRepository
import org.nsh07.pomodoro.ui.AppScreen
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsViewModel
import org.nsh07.pomodoro.ui.theme.TomatoTheme
import org.nsh07.pomodoro.utils.toColor

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ApplicationScope.AppWindow(
    settingsViewModel: SettingsViewModel = koinInject(),
    stateRepository: StateRepository = koinInject()
) =
    Window(
        onCloseRequest = ::exitApplication,
        title = "Tomato"
    ) {
        val settingsState by settingsViewModel.settingsState.collectAsState()
        val isPlus by settingsViewModel.isPlus.collectAsState()

        val windowViewModelStoreOwner = remember {
            object : ViewModelStoreOwner {
                override val viewModelStore = ViewModelStore()
            }
        }

        CompositionLocalProvider(
            LocalViewModelStoreOwner provides windowViewModelStoreOwner
        ) {
            val darkTheme = when (settingsState.theme) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            val seed = settingsState.colorScheme.toColor()

            TomatoTheme(
                darkTheme = darkTheme,
                seedColor = seed,
                blackTheme = settingsState.blackTheme
            ) {
                AppScreen(
                    isAODEnabled = settingsState.aodEnabled,
                    isPlus = isPlus,
                    setTimerFrequency = {
                        stateRepository.timerFrequency = it
                    }
                )
            }
        }
    }