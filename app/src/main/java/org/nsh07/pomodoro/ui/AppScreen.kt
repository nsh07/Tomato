/*
 * Copyright (c) 2025 Nishant Mishra
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
import androidx.compose.material3.ShortNavigationBarArrangement
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
import org.nsh07.pomodoro.MainActivity.Companion.screens
import org.nsh07.pomodoro.ui.settingsScreen.SettingsScreenRoot
import org.nsh07.pomodoro.ui.statsScreen.StatsScreenRoot
import org.nsh07.pomodoro.ui.statsScreen.viewModel.StatsViewModel
import org.nsh07.pomodoro.ui.timerScreen.TimerScreen
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppScreen(
    modifier: Modifier = Modifier,
    timerViewModel: TimerViewModel = viewModel(factory = TimerViewModel.Factory),
    statsViewModel: StatsViewModel = viewModel(factory = StatsViewModel.Factory)
) {
    val uiState by timerViewModel.timerState.collectAsStateWithLifecycle()
    val remainingTime by timerViewModel.time.collectAsStateWithLifecycle()

    val progress by rememberUpdatedState((uiState.totalTime.toFloat() - remainingTime) / uiState.totalTime)

    val layoutDirection = LocalLayoutDirection.current
    val haptic = LocalHapticFeedback.current
    val motionScheme = motionScheme
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    LaunchedEffect(uiState.timerMode) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    val backStack = rememberNavBackStack<Screen>(Screen.Timer)

    Scaffold(
        bottomBar = {
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
                            if (wide) NavigationItemIconPosition.Start
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
                        timerState = uiState,
                        progress = { progress },
                        onAction = timerViewModel::onAction,
                        modifier = modifier.padding(
                            start = contentPadding.calculateStartPadding(layoutDirection),
                            end = contentPadding.calculateEndPadding(layoutDirection),
                            bottom = contentPadding.calculateBottomPadding()
                        )
                    )
                }

                entry<Screen.Settings> {
                    SettingsScreenRoot(
                        modifier = modifier.padding(
                            start = contentPadding.calculateStartPadding(layoutDirection),
                            end = contentPadding.calculateEndPadding(layoutDirection),
                            bottom = contentPadding.calculateBottomPadding()
                        )
                    )
                }

                entry<Screen.Stats> {
                    StatsScreenRoot(
                        viewModel = statsViewModel,
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