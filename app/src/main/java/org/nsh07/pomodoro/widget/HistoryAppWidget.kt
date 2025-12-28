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

package org.nsh07.pomodoro.widget

import android.content.Context
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.TitleBar
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
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.nsh07.pomodoro.MainActivity
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.data.AppStatRepository
import org.nsh07.pomodoro.data.Stat
import org.nsh07.pomodoro.utils.millisecondsToHoursMinutes
import org.nsh07.pomodoro.widget.TomatoWidgetSize.Width4

class HistoryAppWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        val statRepository = AppStatRepository.get(context)
        val history = statRepository.getLastNDaysStats(30).first().reversed()

        provideContent {
            val size = LocalSize.current
            val history = history.takeLast(((size.width.value - 32) / 24).toInt())
            key(size) {
                GlanceTheme {
                    Content(history, history.maxBy { it.totalFocusTime() }.totalFocusTime())
                }
            }
        }
    }

    @Composable
    private fun Content(history: List<Stat>, maxFocus: Long) {
        val context = LocalContext.current
        val size = LocalSize.current
        val scope = rememberCoroutineScope()
        Scaffold(
            titleBar = {
                TitleBar(
                    startIcon = ImageProvider(R.drawable.monitoring),
                    title = context.getString(R.string.focus_history),
                    iconColor = colors.primary,
                    textColor = colors.onPrimaryContainer,
                    actions = {
                        if (size.width >= Width4) {
                            Box(GlanceModifier.padding(horizontal = 16.dp)) {
                                Image(
                                    provider = ImageProvider(R.drawable.refresh),
                                    contentDescription = null,
                                    colorFilter = ColorFilter.tint(colors.onPrimaryContainer),
                                    modifier = GlanceModifier
                                        .cornerRadius(24.dp)
                                        .clickable {
                                            scope.launch { this@HistoryAppWidget.updateAll(context) }
                                        }
                                )
                            }
                        }
                    }
                )
            },
            horizontalPadding = 16.dp,
            modifier = GlanceModifier
                .padding(bottom = 16.dp)
                .clickable(actionStartActivity<MainActivity>())
        ) {
            Column(GlanceModifier.fillMaxSize()) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = millisecondsToHoursMinutes(
                            history.sumOf { it.totalFocusTime() } / history.size,
                            context.getString(R.string.hours_and_minutes_format)
                        ) + " ",
                        style = TextStyle(
                            color = colors.onPrimaryContainer,
                            fontSize = typography.headlineSmall.fontSize,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )
                    if (size.width >= Width4) {
                        Text(
                            text = context.getString(R.string.focus_per_day_avg),
                            style = TextStyle(color = colors.onPrimaryContainer)
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
                                            .background(colors.primary)
                                            .width(20.dp)
                                            .height(
                                                (84 * (it.totalFocusTime().toFloat() / maxFocus)).dp
                                            )
                                            .cornerRadius(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
