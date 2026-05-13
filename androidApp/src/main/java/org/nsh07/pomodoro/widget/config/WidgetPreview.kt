/*
 * Copyright (c) 2026 Nishant Mishra
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

package org.nsh07.pomodoro.widget.config

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMaxBy
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.data.Stat
import org.nsh07.pomodoro.ui.statsScreen.components.HorizontalStackedBar
import org.nsh07.pomodoro.utils.millisecondsToHoursMinutes
import org.nsh07.pomodoro.utils.millisecondsToMinutes
import org.nsh07.pomodoro.widget.HistoryAppWidget.Companion.previewHistoryData
import org.nsh07.pomodoro.widget.TomatoWidgetSize.Height2
import org.nsh07.pomodoro.widget.TomatoWidgetSize.Width4
import java.time.LocalDate

@Composable
fun TodayAppWidgetPreview(
    background: Color,
    onSurface: Color,
    onSurfaceVariant: Color,
    modifier: Modifier = Modifier
) {
    var size by remember { mutableStateOf(DpSize.Zero) }
    val density = LocalDensity.current

    val stat = remember {
        Stat(
            date = LocalDate.of(2026, 3, 12),
            focusTimeQ1 = 1617943,
            focusTimeQ2 = 5704591,
            focusTimeQ3 = 556490,
            focusTimeQ4 = 1200498,
            breakTime = 3939448
        )
    }

    Box(
        contentAlignment = Alignment.TopEnd,
        modifier = modifier
            .background(background, RoundedCornerShape(24.dp))
            .onSizeChanged {
                with(density) {
                    size = DpSize(it.width.toDp(), it.height.toDp())
                }
            }
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                stringResource(R.string.focus),
                style = TextStyle(
                    color = onSurface,
                    fontSize = typography.titleMedium.fontSize
                )
            )

            Text(
                millisecondsToHoursMinutes(
                    stat.totalFocusTime(),
                    stringResource(R.string.hours_and_minutes_format)
                ),
                style = typography.displaySmall,
                color = onSurface,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(Modifier.weight(1f))

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
                                modifier = Modifier.width(((size.width.value - 32f) / 4).dp)
                            ) {
                                Text(
                                    if (it <= 60 * 60 * 1000)
                                        millisecondsToMinutes(
                                            it,
                                            stringResource(R.string.minutes_format)
                                        )
                                    else millisecondsToHoursMinutes(
                                        it,
                                        stringResource(R.string.hours_and_minutes_format)
                                    ),
                                    style = typography.bodyLarge,
                                    color = onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                HorizontalStackedBar(
                    values = values,
                    minutesFormat = stringResource(R.string.minutes_format),
                    hoursMinutesFormat = stringResource(R.string.hours_and_minutes_format),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (size.width >= Width4) {
            Icon(
                painter = painterResource(R.drawable.refresh),
                contentDescription = null,
                tint = onSurface,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
fun HistoryWidgetPreview(
    background: Color,
    onSurface: Color,
    onSurfaceVariant: Color,
    primary: Color,
    modifier: Modifier = Modifier
) {
    var size by remember { mutableStateOf(DpSize.Zero) }
    val density = LocalDensity.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(background, RoundedCornerShape(24.dp))
            .onSizeChanged {
                with(density) {
                    size = DpSize(it.width.toDp(), it.height.toDp())
                }
            }
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .padding(start = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(R.drawable.tomato_logo_notification),
                    contentDescription = "",
                    tint = onSurface,
                )
            }
            Text(
                text = stringResource(R.string.focus_history),
                style = TextStyle(
                    color = onSurface,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                ),
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
            if (size.width >= Width4) {
                Box(Modifier.padding(horizontal = 16.dp)) {
                    Icon(
                        painter = painterResource(R.drawable.refresh),
                        contentDescription = null,
                        tint = onSurface
                    )
                }
            }
        }

        Column(
            Modifier
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                .weight(1f)
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    millisecondsToHoursMinutes(
                        if (previewHistoryData.isEmpty()) 0 else previewHistoryData.sumOf { it.totalFocusTime() } / previewHistoryData.size,
                        stringResource(R.string.hours_and_minutes_format)
                    ) + " ",
                    style = typography.headlineSmall,
                    color = onSurface
                )

                if (size.width >= Width4) {
                    Text(
                        stringResource(R.string.focus_per_day_avg),
                        style = typography.bodyMedium,
                        color = onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 2.8.dp)
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                val maxFocus = remember {
                    previewHistoryData.fastMaxBy { it.totalFocusTime() }?.totalFocusTime() ?: 0L
                }
                previewHistoryData.chunked(10).fastForEachIndexed { baseIndex, it ->
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        it.fastForEachIndexed { index, it ->
                            val flatIndex = baseIndex * 10 + index
                            Box(Modifier.padding(end = if (flatIndex != previewHistoryData.lastIndex) 4.dp else 0.dp)) {
                                Spacer(
                                    Modifier
                                        .width(20.dp)
                                        .height(
                                            (84 * (it.totalFocusTime().toFloat() / maxFocus)).dp
                                        )
                                        .background(primary, RoundedCornerShape(16.dp))
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
fun TimerWidgetPreview(
    opacity: Float,
    background: Color,
    tertiary: Color,
    onTertiary: Color,
    primary: Color,
    onPrimary: Color,
    modifier: Modifier = Modifier
) {
    var size by remember { mutableStateOf(DpSize.Zero) }
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .background(background.copy(alpha = opacity), CircleShape)
            .onSizeChanged {
                with(density) {
                    size = DpSize(it.width.toDp(), it.height.toDp())
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "25:00",
            fontFamily = typography.bodyMedium.fontFamily,
            fontSize = (minOf(256.dp, size.width, size.height).value * 0.25f).sp,
            color = colorScheme.primary
        )

        // Skip / restart button group (top-end) — independent colors
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopEnd) {
            Box(
                Modifier
                    .size(48.dp)
                    .background(tertiary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painterResource(R.drawable.skip_next),
                    null,
                    tint = onTertiary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        // Play / pause button (bottom-start) — foregroundRole bg, headerRole fg
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomStart) {
            Box(
                Modifier
                    .size(60.dp)
                    .background(primary, shapes.large),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painterResource(R.drawable.play),
                    null,
                    tint = onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
