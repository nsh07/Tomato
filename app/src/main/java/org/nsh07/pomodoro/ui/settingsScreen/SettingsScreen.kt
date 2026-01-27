/*
 * Copyright (c) 2025-2026 Nishant Mishra
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

package org.nsh07.pomodoro.ui.settingsScreen

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.SliderState
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import org.nsh07.pomodoro.ui.Screen
import org.nsh07.pomodoro.ui.calculatePaneScaffoldDirective
import org.nsh07.pomodoro.ui.settingsScreen.screens.AboutScreen
import org.nsh07.pomodoro.ui.settingsScreen.screens.AlarmSettings
import org.nsh07.pomodoro.ui.settingsScreen.screens.AppearanceSettings
import org.nsh07.pomodoro.ui.settingsScreen.screens.SettingsMainScreen
import org.nsh07.pomodoro.ui.settingsScreen.screens.TimerSettings
import org.nsh07.pomodoro.ui.settingsScreen.screens.backupRestore.BackupRestoreScreen
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsViewModel
import org.nsh07.pomodoro.ui.theme.CustomColors.topBarColors
import org.nsh07.pomodoro.utils.onBack
import org.nsh07.pomodoro.utils.onTopLevelNavigate


@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun SettingsScreenRoot(
    setShowPaywall: (Boolean) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val backStack = viewModel.backStack

    DisposableEffect(Unit) {
        viewModel.runTextFieldFlowCollection()
        onDispose { viewModel.cancelTextFieldFlowCollection() }
    }

    val focusTimeInputFieldState = viewModel.focusTimeTextFieldState
    val shortBreakTimeInputFieldState = viewModel.shortBreakTimeTextFieldState
    val longBreakTimeInputFieldState = viewModel.longBreakTimeTextFieldState

    val isPlus by viewModel.isPlus.collectAsStateWithLifecycle()
    val serviceRunning by viewModel.serviceRunning.collectAsStateWithLifecycle()

    val settingsState by viewModel.settingsState.collectAsStateWithLifecycle()

    val sessionsSliderState = rememberSaveable(
        saver = SliderState.Saver(
            viewModel.sessionsSliderState.onValueChangeFinished,
            viewModel.sessionsSliderState.valueRange
        )
    ) {
        viewModel.sessionsSliderState
    }

    NavDisplay(
        backStack = backStack,
        onBack = backStack::onBack,
        transitionSpec = {
            (slideInHorizontally(initialOffsetX = { it }))
                .togetherWith(slideOutHorizontally(targetOffsetX = { -it / 4 }) + fadeOut())
        },
        popTransitionSpec = {
            (slideInHorizontally(initialOffsetX = { -it / 4 }) + fadeIn())
                .togetherWith(slideOutHorizontally(targetOffsetX = { it }))
        },
        predictivePopTransitionSpec = {
            (slideInHorizontally(initialOffsetX = { -it / 4 }) + fadeIn())
                .togetherWith(slideOutHorizontally(targetOffsetX = { it }))
        },
        sceneStrategy = rememberListDetailSceneStrategy(
            directive = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo())
        ),
        entryProvider = entryProvider {
            entry<Screen.Settings.Main>(
                metadata = ListDetailSceneStrategy.listPane(
                    detailPlaceholder = { DetailPlaceholder() }
                )
            ) {
                SettingsMainScreen(
                    settingsState = settingsState,
                    contentPadding = contentPadding,
                    currentScreen = backStack.last(),
                    isPlus = isPlus,
                    onAction = viewModel::onAction,
                    onNavigate = backStack::onTopLevelNavigate,
                    setShowPaywall = setShowPaywall,
                    modifier = modifier,
                )
            }

            entry<Screen.Settings.About>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) {
                AboutScreen(
                    contentPadding = contentPadding,
                    isPlus = isPlus,
                    onBack = backStack::onBack
                )
            }

            entry<Screen.Settings.Alarm>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) {
                AlarmSettings(
                    settingsState = settingsState,
                    isPlus = isPlus,
                    contentPadding = contentPadding,
                    onAction = viewModel::onAction,
                    setShowPaywall = setShowPaywall,
                    onBack = backStack::onBack,
                    modifier = modifier,
                )
            }

            entry<Screen.Settings.Appearance>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) {
                AppearanceSettings(
                    settingsState = settingsState,
                    contentPadding = contentPadding,
                    isPlus = isPlus,
                    onAction = viewModel::onAction,
                    setShowPaywall = setShowPaywall,
                    onBack = backStack::onBack,
                    modifier = modifier,
                )
            }

            entry<Screen.Settings.Backup>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) {
                BackupRestoreScreen(
                    contentPadding = contentPadding,
                    onBack = backStack::onBack,
                    modifier = modifier,
                )
            }

            entry<Screen.Settings.Timer>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) {
                TimerSettings(
                    isPlus = isPlus,
                    serviceRunning = serviceRunning,
                    settingsState = settingsState,
                    contentPadding = contentPadding,
                    focusTimeInputFieldState = focusTimeInputFieldState,
                    shortBreakTimeInputFieldState = shortBreakTimeInputFieldState,
                    longBreakTimeInputFieldState = longBreakTimeInputFieldState,
                    sessionsSliderState = sessionsSliderState,
                    onAction = viewModel::onAction,
                    setShowPaywall = setShowPaywall,
                    onBack = backStack::onBack,
                    modifier = modifier,
                )
            }
        },
        modifier = Modifier.background(topBarColors.containerColor)
    )
}
