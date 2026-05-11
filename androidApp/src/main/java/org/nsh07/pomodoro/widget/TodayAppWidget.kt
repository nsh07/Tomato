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
import android.os.Build
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.GlanceTheme.colors
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.material3.ColorProviders
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.nsh07.pomodoro.MainActivity
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.data.Stat
import org.nsh07.pomodoro.data.StatRepository
import org.nsh07.pomodoro.data.WidgetConfigurationDao
import org.nsh07.pomodoro.ui.theme.lightScheme
import org.nsh07.pomodoro.utils.millisecondsToHoursMinutes
import org.nsh07.pomodoro.utils.millisecondsToMinutes
import org.nsh07.pomodoro.widget.TomatoWidgetSize.Height2
import org.nsh07.pomodoro.widget.TomatoWidgetSize.Width4
import org.nsh07.pomodoro.widget.components.GlanceText
import org.nsh07.pomodoro.widget.components.HorizontalStackedBarGlance
import java.time.LocalDate

class TodayAppWidget : GlanceAppWidget(), KoinComponent {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        val statRepository: StatRepository = get()
        val widgetConfigurationDao: WidgetConfigurationDao = get()
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)

        // Fetch once so the very first frame already has the saved config (no flash of defaults),
        // then observe the Flow so any subsequent save recomposes immediately.
        val initialConfig = widgetConfigurationDao.getConfiguration(appWidgetId)
        val configFlow = widgetConfigurationDao.getConfigurationFlow(appWidgetId)

        val stat = statRepository.getTodayStat().first()
            ?: Stat(LocalDate.now(), 0, 0, 0, 0, 0)

        provideContent {
            val config by configFlow.collectAsState(initial = initialConfig)
            val opacity = config?.opacity ?: 1.0f
            val backgroundRole = config?.backgroundRole ?: "onSecondary"
            val foregroundRole = config?.foregroundRole ?: "primary"
            val headerRole = config?.headerRole ?: "onPrimary"
            key(LocalSize.current) {
                GlanceTheme {
                    Content(stat, opacity, backgroundRole, foregroundRole, headerRole)
                }
            }
        }
    }

    @Composable
    private fun Content(
        stat: Stat,
        opacity: Float,
        backgroundRole: String,
        foregroundRole: String,
        headerRole: String
    ) {
        val context = LocalContext.current
        val size = LocalSize.current
        val scope = rememberCoroutineScope()

        val backgroundRoleColorProvider = getWidgetColorProvider(backgroundRole)
        val foregroundRoleColorProvider = getWidgetColorProvider(foregroundRole)
        val headerRoleColorProvider = getWidgetColorProvider(headerRole)

        Box(
            contentAlignment = Alignment.TopEnd,
            modifier = GlanceModifier
                .background(
                    imageProvider = ImageProvider(R.drawable.rounded_24dp),
                    colorFilter = ColorFilter.tint(backgroundRoleColorProvider),
                    alpha = opacity
                )
                .padding(16.dp)
                .clickable(actionStartActivity<MainActivity>())
        ) {
            Column(GlanceModifier.fillMaxSize()) {
                Text(
                    context.getString(R.string.focus),
                    style = TextStyle(
                        color = headerRoleColorProvider,
                        fontSize = typography.titleMedium.fontSize
                    )
                )

                GlanceText(
                    context,
                    millisecondsToHoursMinutes(
                        stat.totalFocusTime(),
                        context.getString(R.string.hours_and_minutes_format)
                    ),
                    typography.displaySmall.fontSize.value,
                    foregroundRoleColorProvider,
                    fontWeight = FontWeight.Bold,
                    isClock = true,
                    modifier = GlanceModifier.padding(top = 8.dp)
                )

                Spacer(GlanceModifier.defaultWeight())

                if (size.height >= Height2) {
                    val values = listOf(
                        stat.focusTimeQ1,
                        stat.focusTimeQ2,
                        stat.focusTimeQ3,
                        stat.focusTimeQ4
                    )
                    if (size.width >= Width4) {
                        Row {
                            values.fastForEach {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = GlanceModifier.width(((size.width.value - 32f) / 4).dp)
                                ) {
                                    GlanceText(
                                        context,
                                        if (it <= 60 * 60 * 1000)
                                            millisecondsToMinutes(
                                                it,
                                                context.getString(R.string.minutes_format)
                                            )
                                        else millisecondsToHoursMinutes(
                                            it,
                                            context.getString(R.string.hours_and_minutes_format)
                                        ),
                                        typography.bodyLarge.fontSize.value,
                                        foregroundRoleColorProvider,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    Spacer(GlanceModifier.height(8.dp))
                    HorizontalStackedBarGlance(
                        values = values,
                        width = size.width - 32.dp,
                        color = foregroundRoleColorProvider
                    )
                }
            }

            if (size.width >= Width4) {
                Image(
                    provider = ImageProvider(R.drawable.refresh),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(headerRoleColorProvider),
                    modifier = GlanceModifier
                        .cornerRadius(12.dp)
                        .clickable {
                            scope.launch { this@TodayAppWidget.updateAll(context) }
                        }
                )
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
    @Preview(widthDp = 400, heightDp = 216)
    @Composable
    private fun ContentPreview() {
        GlanceTheme(colors = ColorProviders(lightScheme)) {
            Box(GlanceModifier.background(Color.White)) {
                Box(
                    GlanceModifier.cornerRadius(32.dp)
                ) {
                    Content(
                        stat = Stat(
                            date = LocalDate.of(2026, 3, 12),
                            focusTimeQ1 = 1617943,
                            focusTimeQ2 = 5704591,
                            focusTimeQ3 = 556490,
                            focusTimeQ4 = 1200498,
                            breakTime = 3939448
                        ),
                        opacity = 1.0f,
                        backgroundRole = "onSecondary",
                        foregroundRole = "primary",
                        headerRole = "onPrimary"
                    )
                }
            }
        }
    }
}
