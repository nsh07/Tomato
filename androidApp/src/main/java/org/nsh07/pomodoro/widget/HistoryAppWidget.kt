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
import androidx.glance.appwidget.SizeMode
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
import androidx.glance.material3.ColorProviders
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontWeight
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.nsh07.pomodoro.MainActivity
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.data.Stat
import org.nsh07.pomodoro.data.StatRepository
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
        val roundedCornersSupported = Build.VERSION.SDK_INT >= 31
        Column(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .then(
                        if (roundedCornersSupported) GlanceModifier.background(colors.widgetBackground)
                        else GlanceModifier.background(
                            ImageProvider(R.drawable.rounded_24dp),
                            colorFilter = ColorFilter.tint(colors.widgetBackground)
                        )
                    )
                    .clickable(actionStartActivity<MainActivity>())
        ) {
            TitleBar(
                startIcon = ImageProvider(R.drawable.tomato_logo_notification),
                title = context.getString(R.string.focus_history),
                actions = {
                    if (size.width >= Width4) {
                        Box(GlanceModifier.padding(horizontal = 16.dp)) {
                            Image(
                                provider = ImageProvider(R.drawable.refresh),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(colors.onSurface),
                                modifier = GlanceModifier
                                    .cornerRadius(24.dp)
                                    .clickable {
                                        scope.launch { this@HistoryAppWidget.updateAll(context) }
                                    }
                            )
                        }
                    }
                },
            )

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
                        colors.onSurface,
                        fontWeight = FontWeight.Bold
                    )

                    if (size.width >= Width4) {
                        GlanceText(
                            context,
                            context.getString(R.string.focus_per_day_avg),
                            typography.bodyMedium.fontSize.value,
                            colors.onSurfaceVariant,
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
                                            .then(
                                                if (roundedCornersSupported)
                                                    GlanceModifier
                                                        .background(colors.primary)
                                                        .cornerRadius(16.dp)
                                                else GlanceModifier.background(
                                                    ImageProvider(R.drawable.rounded_16dp),
                                                    colorFilter = ColorFilter.tint(colors.primary)
                                                )
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalGlancePreviewApi::class)
    @Preview(widthDp = 400, heightDp = 216)
    @Composable
    private fun ContentPreview() {
        val history = listOf(
            Stat(
                date = LocalDate.of(2026, 3, 12),
                focusTimeQ1 = 1617943 + 7200000,
                focusTimeQ2 = 5704591,
                focusTimeQ3 = 556490,
                focusTimeQ4 = 1200498,
                breakTime = 3939448
            ),
            Stat(
                date = LocalDate.of(2026, 3, 13),
                focusTimeQ1 = 1128282 + 7200000,
                focusTimeQ2 = 4590524,
                focusTimeQ3 = 7747202,
                focusTimeQ4 = 1119272,
                breakTime = 311887
            ),
            Stat(
                date = LocalDate.of(2026, 3, 14),
                focusTimeQ1 = 1418079 + 7200000,
                focusTimeQ2 = 8141785,
                focusTimeQ3 = 5208864,
                focusTimeQ4 = 2793210,
                breakTime = 2873581
            ),
            Stat(
                date = LocalDate.of(2026, 3, 15),
                focusTimeQ1 = 38960 + 7200000,
                focusTimeQ2 = 9544172,
                focusTimeQ3 = 2216626,
                focusTimeQ4 = 1424242,
                breakTime = 4635775
            ),
            Stat(
                date = LocalDate.of(2026, 3, 16),
                focusTimeQ1 = 948108 + 7200000,
                focusTimeQ2 = 7715257,
                focusTimeQ3 = 648629,
                focusTimeQ4 = 319655,
                breakTime = 1710029
            ),
            Stat(
                date = LocalDate.of(2026, 3, 17),
                focusTimeQ1 = 1673932 + 7200000,
                focusTimeQ2 = 7368028,
                focusTimeQ3 = 6028910,
                focusTimeQ4 = 2134210,
                breakTime = 2811766
            ),
            Stat(
                date = LocalDate.of(2026, 3, 18),
                focusTimeQ1 = 435688 + 7200000,
                focusTimeQ2 = 9487983,
                focusTimeQ3 = 248276,
                focusTimeQ4 = 913853,
                breakTime = 162869
            ),
            Stat(
                date = LocalDate.of(2026, 3, 19),
                focusTimeQ1 = 1579291 + 7200000,
                focusTimeQ2 = 3743344,
                focusTimeQ3 = 3383617,
                focusTimeQ4 = 3424645,
                breakTime = 3443552
            ),
            Stat(
                date = LocalDate.of(2026, 3, 20),
                focusTimeQ1 = 522247 + 7200000,
                focusTimeQ2 = 7156785,
                focusTimeQ3 = 5190730,
                focusTimeQ4 = 3086522,
                breakTime = 3768831
            ),
            Stat(
                date = LocalDate.of(2026, 3, 21),
                focusTimeQ1 = 310048 + 7200000,
                focusTimeQ2 = 5901959,
                focusTimeQ3 = 441673,
                focusTimeQ4 = 3562958,
                breakTime = 5470220
            ),
            Stat(
                date = LocalDate.of(2026, 3, 22),
                focusTimeQ1 = 1200000 + 7200000,
                focusTimeQ2 = 4000000,
                focusTimeQ3 = 3000000,
                focusTimeQ4 = 1000000,
                breakTime = 2000000
            ),
            Stat(
                date = LocalDate.of(2026, 3, 23),
                focusTimeQ1 = 500000 + 7200000,
                focusTimeQ2 = 8000000,
                focusTimeQ3 = 1000000,
                focusTimeQ4 = 500000,
                breakTime = 1000000
            ),
            Stat(
                date = LocalDate.of(2026, 3, 24),
                focusTimeQ1 = 2000000 + 7200000,
                focusTimeQ2 = 2000000,
                focusTimeQ3 = 2000000,
                focusTimeQ4 = 2000000,
                breakTime = 3000000
            ),
            Stat(
                date = LocalDate.of(2026, 3, 25),
                focusTimeQ1 = 0 + 7200000,
                focusTimeQ2 = 10000000,
                focusTimeQ3 = 0,
                focusTimeQ4 = 0,
                breakTime = 500000
            ),
            Stat(
                date = LocalDate.of(2026, 3, 26),
                focusTimeQ1 = 3000000 + 7200000,
                focusTimeQ2 = 3000000,
                focusTimeQ3 = 3000000,
                focusTimeQ4 = 3000000,
                breakTime = 4000000
            )
        )
        GlanceTheme(colors = ColorProviders(lightScheme)) {
            Box(GlanceModifier.background(Color.White)) {
                Box(
                    GlanceModifier.cornerRadius(32.dp)
                ) {
                    Content(
                        history = history,
                        maxFocus = history.maxBy { it.totalFocusTime() }.totalFocusTime()
                    )
                }
            }
        }
    }
}
