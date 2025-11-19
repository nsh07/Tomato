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

package org.nsh07.pomodoro.ui

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.NavigationItemIconPosition
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarArrangement
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowSizeClass
import org.nsh07.pomodoro.billing.TomatoPlusPaywallDialog
import org.nsh07.pomodoro.service.TimerService
import org.nsh07.pomodoro.ui.settingsScreen.SettingsScreenRoot
import org.nsh07.pomodoro.ui.statsScreen.StatsScreenRoot
import org.nsh07.pomodoro.ui.timerScreen.AlarmDialog
import org.nsh07.pomodoro.ui.timerScreen.TimerScreen
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppScreen(
    isAODEnabled: Boolean,
    isPlus: Boolean,
    setTimerFrequency: (Float) -> Unit,
    modifier: Modifier = Modifier,
    timerViewModel: TimerViewModel = viewModel(factory = TimerViewModel.Factory)
) {
    val context = LocalContext.current

    val uiState by timerViewModel.timerState.collectAsStateWithLifecycle()
    val progress by timerViewModel.progress.collectAsStateWithLifecycle()

    val layoutDirection = LocalLayoutDirection.current
    val motionScheme = motionScheme
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    val backStack = rememberNavBackStack(Screen.Timer)

    if (uiState.alarmRinging)
        AlarmDialog {
            Intent(context, TimerService::class.java).also {
                it.action = TimerService.Actions.STOP_ALARM.toString()
                context.startService(it)
            }
        }

    var showPaywall by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                backStack.last() !is Screen.AOD,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                val wide = remember {
                    windowSizeClass.isWidthAtLeastBreakpoint(
                        WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND
                    )
                }
                ShortNavigationBar(
                    arrangement =
                        if (wide) ShortNavigationBarArrangement.Centered
                        else ShortNavigationBarArrangement.EqualWeight
                ) {
                    mainScreens.forEach {
                        val selected = backStack.last() == it.route
                        ShortNavigationBarItem(
                            selected = selected,
                            onClick = if (it.route != Screen.Timer) { // Ensure the backstack does not accumulate screens
                                {
                                    if (backStack.size < 2) backStack.add(it.route)
                                    else backStack[1] = it.route
                                }
                            } else {
                                { if (backStack.size > 1) backStack.removeAt(1) }
                            },
                            icon = {
                                Crossfade(selected) { selected ->
                                    if (selected) Icon(painterResource(it.selectedIcon), null)
                                    else Icon(painterResource(it.unselectedIcon), null)
                                }
                            },
                            iconPosition =
                                if (wide) NavigationItemIconPosition.Start
                                else NavigationItemIconPosition.Top,
                            label = { Text(stringResource(it.label)) }
                        )
                    }
                }
            }
        }
    ) { contentPadding ->
        SharedTransitionLayout {
            NavDisplay(
                backStack = backStack,
                onBack = backStack::removeLastOrNull,
                transitionSpec = {
                    fadeIn(motionScheme.defaultEffectsSpec())
                        .togetherWith(fadeOut(motionScheme.defaultEffectsSpec()))
                },
                popTransitionSpec = {
                    fadeIn(motionScheme.defaultEffectsSpec())
                        .togetherWith(fadeOut(motionScheme.defaultEffectsSpec()))
                },
                predictivePopTransitionSpec = {
                    fadeIn(motionScheme.defaultEffectsSpec())
                        .togetherWith(fadeOut(motionScheme.defaultEffectsSpec()))
                },
                entryProvider = entryProvider {
                    entry<Screen.Timer> {
                        TimerScreen(
                            timerState = uiState,
                            isPlus = isPlus,
                            progress = { progress },
                            onAction = timerViewModel::onAction,
                            modifier = modifier
                                .padding(
                                    start = contentPadding.calculateStartPadding(layoutDirection),
                                    end = contentPadding.calculateEndPadding(layoutDirection),
                                    bottom = contentPadding.calculateBottomPadding()
                                )
                                .then(
                                    if (isAODEnabled) Modifier.clickable {
                                        if (backStack.size < 2) backStack.add(Screen.AOD)
                                    }
                                    else Modifier
                                ),
                        )
                    }

                    entry<Screen.AOD> {
                        AlwaysOnDisplay(
                            timerState = uiState,
                            progress = { progress },
                            setTimerFrequency = setTimerFrequency,
                            modifier = Modifier
                                .then(
                                    if (isAODEnabled) Modifier.clickable {
                                        if (backStack.size > 1) backStack.removeLastOrNull()
                                    }
                                    else Modifier
                                )
                        )
                    }

                    entry<Screen.Settings.Main> {
                        SettingsScreenRoot(
                            setShowPaywall = { showPaywall = it },
                            modifier = modifier.padding(
                                start = contentPadding.calculateStartPadding(layoutDirection),
                                end = contentPadding.calculateEndPadding(layoutDirection),
                                bottom = contentPadding.calculateBottomPadding()
                            )
                        )
                    }

                    entry<Screen.Stats> {
                        StatsScreenRoot(
                            contentPadding = contentPadding,
                            modifier = modifier.padding(
                                start = contentPadding.calculateStartPadding(layoutDirection),
                                end = contentPadding.calculateEndPadding(layoutDirection),
                                bottom = contentPadding.calculateBottomPadding()
                            )
                        )
                    }
                }
            )
        }
    }

    AnimatedVisibility(
        showPaywall,
        enter = slideInVertically { it },
        exit = slideOutVertically { it }
    ) {
        TomatoPlusPaywallDialog(isPlus = isPlus) { showPaywall = false }
    }
}