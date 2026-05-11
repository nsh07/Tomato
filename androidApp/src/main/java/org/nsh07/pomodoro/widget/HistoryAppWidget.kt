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
import androidx.compose.ui.util.fastForEachIndexed
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
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
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
import org.nsh07.pomodoro.widget.TomatoWidgetSize.Width4
import org.nsh07.pomodoro.widget.components.GlanceText
import java.time.LocalDate

class HistoryAppWidget : GlanceAppWidget(), KoinComponent {
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

        val history = statRepository.getLastNDaysStats(30).first().reversed()

        provideContent {
            val config by configFlow.collectAsState(initial = initialConfig)
            val opacity = config?.opacity ?: 1.0f
            val backgroundRole = config?.backgroundRole ?: "onSecondary"
            val foregroundRole = config?.foregroundRole ?: "primary"
            val headerRole = config?.headerRole ?: "onPrimary"
            val barCornerRadius = config?.barCornerRadius ?: 16
            val size = LocalSize.current
            val history = history.takeLast(((size.width.value - 32) / 24).toInt())
            key(size) {
                GlanceTheme {
                    Content(
                        history,
                        history.maxBy { it.totalFocusTime() }.totalFocusTime(),
                        opacity,
                        backgroundRole,
                        foregroundRole,
                        headerRole,
                        barCornerRadius
                    )
                }
            }
        }
    }

    @Composable
    private fun Content(
        history: List<Stat>,
        maxFocus: Long,
        opacity: Float,
        backgroundRole: String,
        foregroundRole: String,
        headerRole: String,
        barCornerRadius: Int
    ) {
        val context = LocalContext.current
        val size = LocalSize.current
        val scope = rememberCoroutineScope()

        val backgroundRoleColorProvider = getWidgetColorProvider(backgroundRole)
        val foregroundRoleColorProvider = getWidgetColorProvider(foregroundRole)
        val headerRoleColorProvider = getWidgetColorProvider(headerRole)

        Column(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .background(
                        imageProvider = ImageProvider(R.drawable.rounded_24dp),
                        colorFilter = ColorFilter.tint(backgroundRoleColorProvider),
                        alpha = opacity
                    )
                    .clickable(actionStartActivity<MainActivity>())
        ) {
            // Custom Title Bar implementation to ensure icon tinting
            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    provider = ImageProvider(R.drawable.tomato_logo_notification),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(headerRoleColorProvider),
                    modifier = GlanceModifier.size(24.dp)
                )
                Spacer(GlanceModifier.width(8.dp))
                Text(
                    text = context.getString(R.string.focus_history),
                    style = TextStyle(
                        color = headerRoleColorProvider,
                        fontSize = typography.titleSmall.fontSize,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(GlanceModifier.defaultWeight())
                if (size.width >= Width4) {
                    Image(
                        provider = ImageProvider(R.drawable.refresh),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(headerRoleColorProvider),
                        modifier = GlanceModifier
                            .cornerRadius(12.dp)
                            .clickable {
                                scope.launch { this@HistoryAppWidget.updateAll(context) }
                            }
                    )
                }
            }

            Column(
                GlanceModifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    GlanceText(
                        context,
                        millisecondsToHoursMinutes(
                            if (history.isEmpty()) 0 else history.sumOf { it.totalFocusTime() } / history.size,
                            context.getString(R.string.hours_and_minutes_format)
                        ) + " ",
                        typography.headlineSmall.fontSize.value,
                        headerRoleColorProvider,
                        fontWeight = FontWeight.Bold
                    )

                    if (size.width >= Width4) {
                        GlanceText(
                            context,
                            context.getString(R.string.focus_per_day_avg),
                            typography.bodyMedium.fontSize.value,
                            headerRoleColorProvider,
                            isClock = false,
                            modifier = GlanceModifier.padding(bottom = 2.8.dp)
                        )
                    }
                }

                Row(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = GlanceModifier.fillMaxWidth()
                ) {
                    history.chunked(10).fastForEachIndexed { baseIndex, it ->
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            modifier = GlanceModifier.fillMaxHeight()
                        ) {
                            it.fastForEachIndexed { index, it ->
                                val flatIndex = baseIndex * 10 + index
                                Box(GlanceModifier.padding(end = if (flatIndex != history.lastIndex) 4.dp else 0.dp)) {
                                    Spacer(
                                        GlanceModifier
                                            .width(20.dp)
                                            .height(
                                                (84 * (it.totalFocusTime().toFloat() / maxFocus)).dp
                                            )
                                            .background(foregroundRoleColorProvider)
                                            .cornerRadius(barCornerRadius.dp)
                                    )
                                }
                            }
                        }
                    }
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
}
