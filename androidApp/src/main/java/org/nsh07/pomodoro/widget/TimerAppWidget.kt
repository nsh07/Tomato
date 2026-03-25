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

package org.nsh07.pomodoro.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.GlanceTheme.colors
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.components.SquareIconButton
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.size
import androidx.glance.material3.ColorProviders
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.nsh07.pomodoro.MainActivity
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.data.StateRepository
import org.nsh07.pomodoro.service.TimerService
import org.nsh07.pomodoro.ui.theme.lightScheme
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerMode
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerState
import org.nsh07.pomodoro.widget.StartServiceAction.Companion.key
import org.nsh07.pomodoro.widget.components.GlanceText

class TimerAppWidget : GlanceAppWidget(), KoinComponent {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        val stateRepository: StateRepository = get()
        provideContent {
            val timerState by stateRepository.timerState.collectAsState()
            GlanceTheme {
                Content(timerState)
            }
        }
    }

    @Composable
    private fun Content(timerState: TimerState) {
        val size = LocalSize.current
        val context = LocalContext.current
        val circleSize = minOf(256.dp, size.width, size.height)
        val breakMode =
            timerState.timerMode == TimerMode.SHORT_BREAK || timerState.timerMode == TimerMode.LONG_BREAK

        val secondaryButtonColor = if (!breakMode) colors.tertiary else colors.primary
        val onSecondaryButtonColor = if (!breakMode) colors.onTertiary else colors.onPrimary

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color.Transparent)
                .clickable(actionStartActivity<MainActivity>()),
            contentAlignment = Alignment.Center
        ) {
            Box(contentAlignment = Alignment.TopEnd) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = GlanceModifier
                        .size(circleSize)
                        .background(
                            ImageProvider(R.drawable.rounded_full),
                            colorFilter = ColorFilter.tint(colors.widgetBackground)
                        )
                ) {
                    val clockHeight = (circleSize.value * 0.25f)
                    if (timerState.alarmRinging) {
                        Image(
                            ImageProvider(R.drawable.alarm),
                            contentDescription = context.getString(R.string.stop_alarm),
                            colorFilter = ColorFilter.tint(colors.primary),
                            modifier = GlanceModifier.size(clockHeight.dp)
                        )
                    } else {
                        GlanceText(
                            context,
                            timerState.timeStr,
                            clockHeight,
                            if (!breakMode) colors.primary
                            else colors.tertiary
                        )
                    }
                }

                if (!timerState.alarmRinging) {
                    Row(
                        GlanceModifier
                            .background(
                                ImageProvider(R.drawable.rounded_24dp),
                                colorFilter = ColorFilter.tint(secondaryButtonColor)
                            )
                    ) {
                        if (timerState.timerRunning)
                            CircleIconButton(
                                imageProvider = ImageProvider(R.drawable.restart),
                                contentDescription = context.getString(R.string.restart),
                                onClick = actionRunCallback<StartServiceAction>(
                                    actionParametersOf(key to TimerService.Actions.RESET)
                                ),
                                backgroundColor = secondaryButtonColor,
                                contentColor = onSecondaryButtonColor
                            )

                        CircleIconButton(
                            imageProvider = ImageProvider(R.drawable.skip_next),
                            contentDescription = context.getString(R.string.skip_to_next),
                            onClick = actionRunCallback<StartServiceAction>(
                                actionParametersOf(key to TimerService.Actions.SKIP)
                            ),
                            backgroundColor = secondaryButtonColor,
                            contentColor = onSecondaryButtonColor
                        )
                    }
                }

                Box(
                    contentAlignment = Alignment.BottomStart,
                    modifier = GlanceModifier.size(circleSize)
                ) {
                    SquareIconButton(
                        imageProvider =
                            if (timerState.alarmRinging) {
                                ImageProvider(R.drawable.stop)
                            } else {
                                if (!timerState.timerRunning) ImageProvider(R.drawable.play)
                                else ImageProvider(R.drawable.pause)
                            },
                        contentDescription = context.getString(R.string.play),
                        onClick = if (timerState.alarmRinging) {
                            actionRunCallback<StartServiceAction>(
                                actionParametersOf(key to TimerService.Actions.STOP_ALARM)
                            )
                        } else {
                            actionRunCallback<StartServiceAction>(
                                actionParametersOf(key to TimerService.Actions.TOGGLE)
                            )
                        },
                        backgroundColor =
                            if (breakMode)
                                colors.tertiary
                            else colors.primary,
                        contentColor =
                            if (breakMode)
                                colors.onTertiary
                            else colors.onPrimary
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalGlancePreviewApi::class)
    @Preview(widthDp = 196, heightDp = 196)
    @Composable
    private fun ContentPreview() {
        GlanceTheme(colors = ColorProviders(lightScheme)) {
            Box(GlanceModifier.background(Color.White)) {
                Content(
                    timerState = TimerState()
                )
            }
        }
    }
}
