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
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarDefaults.ScreenOffset
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.zIndex
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
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerMode
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
    val systemBarsInsets = WindowInsets.systemBars.asPaddingValues()
    val cutoutInsets = WindowInsets.displayCutout.asPaddingValues()

    val backStack = rememberNavBackStack(Screen.Timer)
    val toolbarScrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(
        FloatingToolbarExitDirection.Bottom
    )

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
                enter = slideInVertically(motionScheme.slowSpatialSpec()) { it },
                exit = slideOutVertically(motionScheme.slowSpatialSpec()) { it }
            ) {
                val wide = remember {
                    windowSizeClass.isWidthAtLeastBreakpoint(
                        WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND
                    )
                }

                val primary by animateColorAsState(
                    if (uiState.timerMode == TimerMode.FOCUS) colorScheme.primary else colorScheme.tertiary
                )
                val onPrimary by animateColorAsState(
                    if (uiState.timerMode == TimerMode.FOCUS) colorScheme.onPrimary else colorScheme.onTertiary
                )
                val primaryContainer by animateColorAsState(
                    if (uiState.timerMode == TimerMode.FOCUS) colorScheme.primaryContainer else colorScheme.tertiaryContainer
                )
                val onPrimaryContainer by animateColorAsState(
                    if (uiState.timerMode == TimerMode.FOCUS) colorScheme.onPrimaryContainer else colorScheme.onTertiaryContainer
                )

                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            start = cutoutInsets.calculateStartPadding(layoutDirection),
                            end = cutoutInsets.calculateEndPadding(layoutDirection)
                        ),
                    Alignment.Center
                ) {
                    HorizontalFloatingToolbar(
                        expanded = true,
                        scrollBehavior = toolbarScrollBehavior,
                        colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(
                            toolbarContainerColor = primaryContainer,
                            toolbarContentColor = onPrimaryContainer
                        ),
                        modifier = Modifier
                            .padding(
                                top = ScreenOffset,
                                bottom = systemBarsInsets.calculateBottomPadding()
                                        + ScreenOffset
                            )
                            .zIndex(1f)
                    ) {
                        mainScreens.fastForEach { item ->
                            val selected by remember { derivedStateOf { backStack.lastOrNull() == item.route } }
                            TooltipBox(
                                positionProvider =
                                    TooltipDefaults.rememberTooltipPositionProvider(
                                        TooltipAnchorPosition.Above
                                    ),
                                tooltip = { PlainTooltip { Text(stringResource(item.label)) } },
                                state = rememberTooltipState(),
                            ) {
                                ToggleButton(
                                    checked = selected,
                                    onCheckedChange = if (item.route != Screen.Timer) { // Ensure the backstack does not accumulate screens
                                        {
                                            if (backStack.size < 2) backStack.add(item.route)
                                            else backStack[1] = item.route
                                        }
                                    } else {
                                        { if (backStack.size > 1) backStack.removeAt(1) }
                                    },
                                    colors = ToggleButtonDefaults.toggleButtonColors(
                                        containerColor = primaryContainer,
                                        contentColor = onPrimaryContainer,
                                        checkedContainerColor = primary,
                                        checkedContentColor = onPrimary
                                    ),
                                    shapes = ToggleButtonDefaults.shapes(
                                        CircleShape,
                                        CircleShape,
                                        CircleShape
                                    ),
                                    modifier = Modifier.height(56.dp)
                                ) {
                                    Row {
                                        Crossfade(selected) {
                                            if (it) Icon(
                                                painterResource(item.selectedIcon),
                                                null
                                            )
                                            else Icon(painterResource(item.unselectedIcon), null)
                                        }
                                        AnimatedVisibility(
                                            visible = selected || wide,
                                            enter = expandHorizontally(motionScheme.defaultSpatialSpec()),
                                            exit = shrinkHorizontally(motionScheme.defaultSpatialSpec())
                                        ) {
                                            Text(
                                                text = stringResource(item.label),
                                                fontSize = 16.sp,
                                                lineHeight = 24.sp,
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Clip,
                                                modifier = Modifier.padding(start = ButtonDefaults.IconSpacing)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        modifier = modifier
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
                            contentPadding = contentPadding,
                            progress = { progress },
                            onAction = timerViewModel::onAction,
                            modifier = if (isAODEnabled) Modifier.clickable {
                                if (backStack.size < 2) backStack.add(Screen.AOD)
                            } else Modifier
                        )
                    }

                    entry<Screen.AOD> {
                        AlwaysOnDisplay(
                            timerState = uiState,
                            progress = { progress },
                            setTimerFrequency = setTimerFrequency,
                            modifier = if (isAODEnabled) Modifier.clickable {
                                if (backStack.size > 1) backStack.removeLastOrNull()
                            } else Modifier
                        )
                    }

                    entry<Screen.Settings.Main> {
                        SettingsScreenRoot(
                            setShowPaywall = { showPaywall = it },
                            contentPadding = contentPadding
                        )
                    }

                    entry<Screen.Stats> {
                        StatsScreenRoot(contentPadding = contentPadding)
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