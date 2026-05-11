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
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.components.SquareIconButton
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.material3.ColorProviders
import androidx.glance.unit.ColorProvider
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.nsh07.pomodoro.MainActivity
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.data.StateRepository
import org.nsh07.pomodoro.data.WidgetConfigurationDao
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
        val widgetConfigurationDao: WidgetConfigurationDao = get()
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)

        // Fetch once so the very first frame already has the saved config (no flash of defaults),
        // then observe the Flow so any subsequent save recomposes immediately.
        val initialConfig = widgetConfigurationDao.getConfiguration(appWidgetId)
        val configFlow = widgetConfigurationDao.getConfigurationFlow(appWidgetId)

        provideContent {
            val timerState by stateRepository.timerState.collectAsState()
            val config by configFlow.collectAsState(initial = initialConfig)
            val opacity = config?.opacity ?: 1.0f
            val backgroundRole = config?.backgroundRole ?: "onSecondary"
            val foregroundRole = config?.foregroundRole ?: "primary"
            val headerRole = config?.headerRole ?: "onPrimary"
            val skipButtonRole = config?.skipButtonRole ?: "tertiary"
            val onSkipButtonRole = config?.onSkipButtonRole ?: "onTertiary"
            GlanceTheme {
                Content(timerState, opacity, backgroundRole, foregroundRole, headerRole, skipButtonRole, onSkipButtonRole)
            }
        }
    }

    @Composable
    private fun Content(
        timerState: TimerState,
        opacity: Float,
        backgroundRole: String,
        foregroundRole: String,
        headerRole: String,
        skipButtonRole: String,
        onSkipButtonRole: String
    ) {
        val size = LocalSize.current
        val context = LocalContext.current
        val circleSize = minOf(256.dp, size.width - 8.dp, size.height - 8.dp)
        val breakMode =
            timerState.timerMode == TimerMode.SHORT_BREAK || timerState.timerMode == TimerMode.LONG_BREAK

        val backgroundRoleColorProvider = getWidgetColorProvider(backgroundRole)
        val foregroundRoleColorProvider = getWidgetColorProvider(foregroundRole)
        val headerRoleColorProvider = getWidgetColorProvider(headerRole)
        val skipButtonRoleColorProvider = getWidgetColorProvider(skipButtonRole)
        val onSkipButtonRoleColorProvider = getWidgetColorProvider(onSkipButtonRole)

        // Play/pause button uses foregroundRole for background, headerRole for icons
        val buttonColor = foregroundRoleColorProvider
        val onButtonColor = headerRoleColorProvider
        // Skip/restart button group has independent colors
        val skipColor = skipButtonRoleColorProvider
        val onSkipColor = onSkipButtonRoleColorProvider

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(4.dp)
                .appWidgetBackground()
                .background(ColorProvider(Color.Transparent))
                .cornerRadius(0.dp)
                .clickable(actionStartActivity<MainActivity>()),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = GlanceModifier.size(circleSize),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(
                            imageProvider = ImageProvider(R.drawable.rounded_full),
                            colorFilter = ColorFilter.tint(backgroundRoleColorProvider),
                            alpha = opacity
                        )
                ) {}

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = GlanceModifier.fillMaxSize()
                ) {
                    val clockHeight = (circleSize.value * 0.25f)
                    if (timerState.alarmRinging) {
                        Image(
                            ImageProvider(R.drawable.alarm),
                            contentDescription = context.getString(R.string.stop_alarm),
                            colorFilter = ColorFilter.tint(foregroundRoleColorProvider),
                            modifier = GlanceModifier.size(clockHeight.dp)
                        )
                    } else {
                        GlanceText(
                            context,
                            timerState.timeStr,
                            clockHeight,
                            foregroundRoleColorProvider
                        )
                    }
                }

                if (!timerState.alarmRinging) {
                    Box(
                        contentAlignment = Alignment.TopEnd,
                        modifier = GlanceModifier.fillMaxSize()
                    ) {
                        Row(
                            modifier = GlanceModifier
                                .background(
                                    imageProvider = ImageProvider(R.drawable.rounded_24dp),
                                    colorFilter = ColorFilter.tint(skipColor)
                                )
                        ) {
                            if (timerState.timerRunning)
                                CircleIconButton(
                                    imageProvider = ImageProvider(R.drawable.restart),
                                    contentDescription = context.getString(R.string.restart),
                                    onClick = actionRunCallback<StartServiceAction>(
                                        actionParametersOf(key to TimerService.Actions.RESET)
                                    ),
                                    backgroundColor = skipColor,
                                    contentColor = onSkipColor
                                )

                            CircleIconButton(
                                imageProvider = ImageProvider(R.drawable.skip_next),
                                contentDescription = context.getString(R.string.skip_to_next),
                                onClick = actionRunCallback<StartServiceAction>(
                                    actionParametersOf(key to TimerService.Actions.SKIP)
                                ),
                                backgroundColor = skipColor,
                                contentColor = onSkipColor
                            )
                        }
                    }
                }

                Box(
                    contentAlignment = Alignment.BottomStart,
                    modifier = GlanceModifier.fillMaxSize()
                ) {
                    SquareIconButton(
                        imageProvider =
                            if (timerState.alarmRinging) {
                                ImageProvider(R.drawable.stop)
                            } else {
                                if (!timerState.timerRunning) ImageProvider(R.drawable.play)
                                else ImageProvider(R.drawable.pause)
                            },
                        contentDescription = context.getString(R.string.start),
                        onClick = if (timerState.alarmRinging) {
                            actionRunCallback<StartServiceAction>(
                                actionParametersOf(key to TimerService.Actions.STOP_ALARM)
                            )
                        } else {
                            actionRunCallback<StartServiceAction>(
                                actionParametersOf(key to TimerService.Actions.TOGGLE)
                            )
                        },
                        backgroundColor = buttonColor,
                        contentColor = onButtonColor
                    )
                }
            }
        }
    }

    @Composable
    private fun getWidgetColorProvider(role: String): ColorProvider {
        return when (role) {
            "black" -> ColorProvider(Color.Black)
            "primary" -> colors.primary
            "onPrimary" -> colors.onPrimary
            "primaryContainer" -> colors.primaryContainer
            "onPrimaryContainer" -> colors.onPrimaryContainer
            "secondary" -> colors.secondary
            "onSecondary" -> colors.onSecondary
            "secondaryContainer" -> colors.secondaryContainer
            "onSecondaryContainer" -> colors.onSecondaryContainer
            "tertiary" -> colors.tertiary
            "onTertiary" -> colors.onTertiary
            "tertiaryContainer" -> colors.tertiaryContainer
            "onTertiaryContainer" -> colors.onTertiaryContainer
            "error" -> colors.error
            "onError" -> colors.onError
            "errorContainer" -> colors.errorContainer
            "onErrorContainer" -> colors.onErrorContainer
            "surface" -> colors.surface
            "onSurface" -> colors.onSurface
            "surfaceVariant" -> colors.surfaceVariant
            "onSurfaceVariant" -> colors.onSurfaceVariant
            "outline" -> colors.outline
            "background" -> colors.background
            "onBackground" -> colors.onBackground
            "inverseSurface" -> colors.inverseSurface
            "inverseOnSurface" -> colors.inverseOnSurface
            "inversePrimary" -> colors.inversePrimary
            "white" -> ColorProvider(Color.White)
            else -> colors.surface
        }
    }

    @OptIn(ExperimentalGlancePreviewApi::class)
    @Preview(widthDp = 196, heightDp = 196)
    @Composable
    private fun ContentPreview() {
        GlanceTheme(colors = ColorProviders(lightScheme)) {
            Box(GlanceModifier.background(Color.White)) {
                Content(
                    timerState = TimerState(),
                    opacity = 1.0f,
                    backgroundRole = "onSecondary",
                    foregroundRole = "primary",
                    headerRole = "onPrimary",
                    skipButtonRole = "tertiary",
                    onSkipButtonRole = "onTertiary"
                )
            }
        }
    }
}
