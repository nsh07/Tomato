package org.nsh07.pomodoro.ui

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
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
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowSizeClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.nsh07.pomodoro.MainActivity.Companion.screens
import org.nsh07.pomodoro.ui.settingsScreen.SettingsScreen
import org.nsh07.pomodoro.ui.statsScreen.StatsScreen
import org.nsh07.pomodoro.ui.timerScreen.TimerScreen
import org.nsh07.pomodoro.ui.viewModel.UiViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppScreen(
    modifier: Modifier = Modifier,
    viewModel: UiViewModel = viewModel(factory = UiViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val remainingTime by viewModel.time.collectAsStateWithLifecycle()

    val progress by rememberUpdatedState((uiState.totalTime.toFloat() - remainingTime) / uiState.totalTime)
    var showBrandTitle by remember { mutableStateOf(true) }

    val layoutDirection = LocalLayoutDirection.current
    val haptic = LocalHapticFeedback.current
    val motionScheme = motionScheme
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            delay(1500)
            showBrandTitle = false
        }
    }

    LaunchedEffect(uiState.timerMode) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    val backStack = rememberNavBackStack<Screen>(Screen.Timer)

    Scaffold(
        bottomBar = {
            ShortNavigationBar {
                screens.forEach {
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
                            if (windowSizeClass.isWidthAtLeastBreakpoint(
                                    WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND
                                )
                            ) NavigationItemIconPosition.Start
                            else NavigationItemIconPosition.Top,
                        label = { Text(it.label) }
                    )
                }
            }
        }
    ) { contentPadding ->
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            transitionSpec = {
                ContentTransform(
                    fadeIn(motionScheme.defaultEffectsSpec()),
                    fadeOut(motionScheme.defaultEffectsSpec())
                )
            },
            popTransitionSpec = {
                ContentTransform(
                    fadeIn(motionScheme.defaultEffectsSpec()),
                    fadeOut(motionScheme.defaultEffectsSpec())
                )
            },
            predictivePopTransitionSpec = {
                ContentTransform(
                    fadeIn(motionScheme.defaultEffectsSpec()),
                    fadeOut(motionScheme.defaultEffectsSpec()) +
                            scaleOut(targetScale = 0.7f),
                )
            },
            entryProvider = entryProvider {
                entry<Screen.Timer> {
                    TimerScreen(
                        uiState = uiState,
                        showBrandTitle = showBrandTitle,
                        progress = { progress },
                        resetTimer = viewModel::updateTimerConstants,
                        skipTimer = viewModel::skipTimer,
                        toggleTimer = viewModel::toggleTimer,
                        modifier = modifier.padding(
                            start = contentPadding.calculateStartPadding(layoutDirection),
                            end = contentPadding.calculateEndPadding(layoutDirection),
                            bottom = contentPadding.calculateBottomPadding()
                        )
                    )
                }

                entry<Screen.Settings> {
                    SettingsScreen()
                }

                entry<Screen.Stats> {
                    StatsScreen()
                }
            }
        )
    }
}