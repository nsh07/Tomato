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

package org.nsh07.pomodoro.ui.timerScreen

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDragHandle
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AdaptStrategy
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffold
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffoldDefaults
import androidx.compose.material3.adaptive.layout.rememberPaneExpansionState
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import org.nsh07.pomodoro.data.Topic
import org.nsh07.pomodoro.ui.androidSystemGestureExclusion
import org.nsh07.pomodoro.ui.settingsScreen.screens.sampleTopics
import org.nsh07.pomodoro.ui.theme.SeededTheme
import org.nsh07.pomodoro.ui.theme.TomatoTheme
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerAction
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerMode
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerState

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3AdaptiveApi::class
)
@Composable
fun SharedTransitionScope.TimerScreen(
    timerState: TimerState,
    topics: List<Topic>,
    currentTopic: Topic,
    isPlus: Boolean,
    contentPadding: PaddingValues,
    progress: () -> Float,
    onAction: (TimerAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val navigator = rememberSupportingPaneScaffoldNavigator(
        adaptStrategies = SupportingPaneScaffoldDefaults.adaptStrategies(supportingPaneAdaptStrategy = AdaptStrategy.Hide)
    )
    val expansionState = rememberPaneExpansionState()

    SeededTheme(seedColor = currentTopic.color) {
        SupportingPaneScaffold(
            directive = navigator.scaffoldDirective,
            scaffoldState = navigator.scaffoldState,
            mainPane = {
                AnimatedPane {
                    TimerMainPane(
                        timerState = timerState,
                        currentTopic = currentTopic,
                        topics = topics,
                        isPlus = isPlus,
                        contentPadding = contentPadding,
                        progress = progress,
                        onAction = onAction,
                        modifier = modifier
                    )
                }
            },
            supportingPane = {
                AnimatedPane {
                    TimerSupportingPane(
                        timerState = timerState,
                        currentTopic = currentTopic,
                        contentPadding = contentPadding
                    )
                }
            },
            paneExpansionDragHandle = {
                val interactionSource =
                    remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                VerticalDragHandle(
                    modifier = Modifier
                        .paneExpansionDraggable(
                            expansionState,
                            LocalMinimumInteractiveComponentSize.current,
                            interactionSource
                        )
                        .androidSystemGestureExclusion()
                )
            },
            paneExpansionState = expansionState
        )
    }
}

@Preview(
    showSystemUi = true,
    device = Devices.PIXEL_9_PRO
)
@Composable
fun TimerScreenPreview() {
    val timerState = TimerState(
        timeStr = "03:34", nextTimeStr = "5:00", timerMode = TimerMode.FOCUS, timerRunning = true
    )
    TomatoTheme {
        Surface {
            SharedTransitionLayout {
                TimerScreen(
                    timerState = timerState,
                    topics = sampleTopics,
                    currentTopic = sampleTopics[5],
                    isPlus = true,
                    contentPadding = PaddingValues(),
                    { 0.3f },
                    {}
                )
            }
        }
    }
}
