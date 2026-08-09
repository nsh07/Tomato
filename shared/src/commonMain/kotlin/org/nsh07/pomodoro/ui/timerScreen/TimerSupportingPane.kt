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

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.nsh07.pomodoro.data.Topic
import org.nsh07.pomodoro.ui.theme.CustomColors.detailPaneTopBarColors
import org.nsh07.pomodoro.ui.theme.CustomColors.listItemColors
import org.nsh07.pomodoro.ui.theme.LocalAppFonts
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.segmentedListItemShapes
import org.nsh07.pomodoro.ui.theme.TomatoTheme
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerMode
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerState
import org.nsh07.pomodoro.utils.millisecondsToStr
import tomato.shared.generated.resources.Res
import tomato.shared.generated.resources.check_circle_40dp
import tomato.shared.generated.resources.focus
import tomato.shared.generated.resources.in_progress_40dp
import tomato.shared.generated.resources.long_break
import tomato.shared.generated.resources.not_started_40dp
import tomato.shared.generated.resources.short_break
import tomato.shared.generated.resources.up_next

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TimerSupportingPane(
    timerState: TimerState,
    currentTopic: Topic,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val isFocus = timerState.timerMode == TimerMode.FOCUS
    LazyColumn(
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .background(detailPaneTopBarColors.containerColor)
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.up_next),
                        fontFamily = LocalAppFonts.current.topBarTitle,
                        maxLines = 1
                    )
                },
                subtitle = {},
                windowInsets = WindowInsets(),
                colors = detailPaneTopBarColors
            )
        }
        items(timerState.totalFocusCount) {
            val currentSession =
                it + 1 == timerState.currentFocusCount // currentFocusCount is 1-indexed
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                SegmentedListItem(
                    onClick = {},
                    enabled = it + 1 >= timerState.currentFocusCount,
                    selected = currentSession && isFocus,
                    shapes = segmentedListItemShapes(0, 2),
                    colors = listItemColors,
                    leadingContent = {
                        AnimatedContent(
                            if (currentSession && isFocus) 1
                            else if (it < timerState.currentFocusCount) 2
                            else 3
                        ) { show ->
                            when (show) {
                                1 -> Icon(
                                    painterResource(Res.drawable.in_progress_40dp),
                                    null
                                )

                                2 -> Icon(
                                    painterResource(Res.drawable.check_circle_40dp),
                                    null
                                )

                                else -> Icon(
                                    painterResource(Res.drawable.not_started_40dp),
                                    null
                                )
                            }
                        }
                    },
                    supportingContent = {
                        Text(
                            millisecondsToStr(currentTopic.focusTime),
                            maxLines = 1
                        )
                    }
                ) {
                    Text(
                        stringResource(Res.string.focus),
                        maxLines = 1
                    )
                }

                SegmentedListItem(
                    onClick = {},
                    enabled = it + 1 >= timerState.currentFocusCount,
                    selected = currentSession && !isFocus,
                    shapes = segmentedListItemShapes(1, 2),
                    colors = listItemColors,
                    leadingContent = {
                        AnimatedContent(
                            if (currentSession && !isFocus) 1
                            else if (it + 1 < timerState.currentFocusCount) 2
                            else 3
                        ) { show ->
                            when (show) {
                                1 -> Icon(
                                    painterResource(Res.drawable.in_progress_40dp),
                                    null
                                )

                                2 -> Icon(
                                    painterResource(Res.drawable.check_circle_40dp),
                                    null
                                )

                                else -> Icon(
                                    painterResource(Res.drawable.not_started_40dp),
                                    null
                                )
                            }
                        }
                    },
                    supportingContent = {
                        Text(
                            if (it != timerState.totalFocusCount - 1) millisecondsToStr(
                                currentTopic.shortBreakTime
                            )
                            else millisecondsToStr(currentTopic.longBreakTime),
                            maxLines = 1
                        )
                    }
                ) {
                    Text(
                        if (it != timerState.totalFocusCount - 1) stringResource(Res.string.short_break)
                        else stringResource(Res.string.long_break),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Preview(
    showSystemUi = true,
    device = Devices.PIXEL_9_PRO
)
@Composable
fun TimerSupportingPanePreview() {
    val timerState = TimerState(
        timeStr = "03:34",
        nextTimeStr = "5:00",
        timerMode = TimerMode.FOCUS,
        timerRunning = true,
        totalFocusCount = 4,
        currentFocusCount = 1
    )
    TomatoTheme {
        Surface {
            TimerSupportingPane(
                timerState,
                Topic.defaultTopic,
                contentPadding = PaddingValues()
            )
        }
    }
}
