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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.isTraySupported
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import kotlinx.coroutines.flow.update
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.nsh07.pomodoro.data.StateRepository
import org.nsh07.pomodoro.ui.AppScreen
import org.nsh07.pomodoro.ui.LocalContentWindowInsets
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsViewModel
import org.nsh07.pomodoro.ui.theme.TomatoTheme
import org.nsh07.pomodoro.utils.OS
import org.nsh07.pomodoro.utils.currentOS
import tomato.shared.generated.resources.Res
import tomato.shared.generated.resources.app_name
import tomato.shared.generated.resources.logo

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ApplicationScope.AppWindow(
    settingsViewModel: SettingsViewModel = koinInject(),
    stateRepository: StateRepository = koinInject()
) {
    val isMacOS = currentOS == OS.MACOS
    val windowState: WindowState = koinInject()
    val windowVisible by stateRepository.windowVisible.collectAsState()

    val settingsState by settingsViewModel.settingsState.collectAsState()

    val windowViewModelStoreOwner = remember {
        object : ViewModelStoreOwner {
            override val viewModelStore = ViewModelStore()
        }
    }

    val customWindowDecorsEnabled = settingsState.customWindowDecor && !isMacOS

    CompositionLocalProvider(
        LocalViewModelStoreOwner provides windowViewModelStoreOwner,
        LocalContentWindowInsets provides {
            WindowInsets(
                top = if (settingsState.customWindowDecor)
                    if (isMacOS) 28.dp else 32.dp
                else 0.dp
            )
        }
    ) {
        if (isTraySupported) {
            AppSystemTray()
        }

        if (windowVisible) {
            key(settingsState.customWindowDecor) {
                Window(
                    state = windowState,
                    onCloseRequest = { stateRepository.windowVisible.update { false } },
                    title = stringResource(Res.string.app_name),
                    icon = painterResource(Res.drawable.logo),
                    undecorated = customWindowDecorsEnabled,
                    transparent = customWindowDecorsEnabled,
                    alwaysOnTop = BuildKonfig.DEBUG
                ) {
                    if (isMacOS && settingsState.customWindowDecor) {
                        window.rootPane.apply {
                            putClientProperty("apple.awt.fullWindowContent", true)
                            putClientProperty("apple.awt.transparentTitleBar", true)
                            putClientProperty("apple.awt.windowTitleVisible", false)
                        }
                    }

                    val darkTheme = when (settingsState.theme) {
                        "dark" -> true
                        "light" -> false
                        else -> isSystemInDarkTheme()
                    }

                    val seed = settingsState.colorScheme

                    TomatoTheme(
                        darkTheme = darkTheme,
                        seedColor = seed,
                        blackTheme = settingsState.blackTheme
                    ) {
                        Box(
                            Modifier
                                .then(
                                    if (customWindowDecorsEnabled && windowState.placement == WindowPlacement.Floating)
                                        Modifier.clip(shapes.small)
                                    else Modifier
                                )
                                .background(colorScheme.surface)
                        ) {
                            AppScreen(
                                isAODEnabled = settingsState.aodEnabled,
                                setTimerFrequency = {
                                    stateRepository.timerFrequency = it
                                }
                            )

                            AnimatedVisibility(
                                windowState.placement != WindowPlacement.Fullscreen &&
                                        settingsState.customWindowDecor,
                                enter = expandVertically(),
                                exit = shrinkVertically()
                            ) {
                                AppTitleBar(
                                    windowFloating = windowState.placement == WindowPlacement.Floating,
                                    onMinimize = { windowState.isMinimized = true },
                                    onMaximizeRestore = {
                                        if (windowState.placement == WindowPlacement.Floating)
                                            windowState.placement = WindowPlacement.Maximized
                                        else windowState.placement = WindowPlacement.Floating
                                    },
                                    onClose = { stateRepository.windowVisible.update { false } }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}