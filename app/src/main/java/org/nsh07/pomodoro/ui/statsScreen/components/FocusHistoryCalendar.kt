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

package org.nsh07.pomodoro.ui.statsScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import org.nsh07.pomodoro.data.Stat
import org.nsh07.pomodoro.ui.theme.TomatoTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.random.Random

val CALENDAR_CELL_SIZE = 40.dp
val CALENDAR_CELL_HORIZONTAL_GAP = 2.dp
val CALENDAR_CELL_VERTICAL_GAP = 4.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FocusHistoryCalendar(
    data: List<Stat?>,
    averageRankList: List<Int>,
    modifier: Modifier = Modifier,
    size: Dp = CALENDAR_CELL_SIZE,
    horizontalGap: Dp = CALENDAR_CELL_HORIZONTAL_GAP,
    verticalGap: Dp = CALENDAR_CELL_VERTICAL_GAP
) {
    val locale = Locale.getDefault()
    val shapes = shapes
    val last = data.lastOrNull { it != null }

    val daysOfWeek = remember(locale) {
        DayOfWeek.entries.map {
            it.getDisplayName(
                TextStyle.SHORT,
                locale
            )
        }
    } // Names of the 7 days of the week in the current locale

    val groupedData = remember(data) {
        data.chunked(7)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(verticalGap),
        modifier = modifier
            .fillMaxWidth()
            .background(colorScheme.surfaceContainer, shapes.largeIncreased)
            .horizontalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(horizontalGap)) {
            daysOfWeek.fastForEach {
                Text(
                    text = it,
                    textAlign = TextAlign.Center,
                    style = typography.bodySmall,
                    color = colorScheme.outline,
                    modifier = Modifier.width(size)
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(verticalGap)) {
            groupedData.fastForEachIndexed { baseIndex, items ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(horizontalGap),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(size)
                ) {
                    items.fastForEachIndexed { index, it ->
                        val background = remember(it) { (it?.totalFocusTime() ?: 0) > 0 }
                        val currentMonth =
                            remember(it, last) { it?.date?.month == last?.date?.month }

                        val shape = remember(data, background) {
                            if (background) {
                                val next =
                                    (data.getOrNull(baseIndex * 7 + index + 1)?.totalFocusTime()
                                        ?: 0) > 0
                                val previous =
                                    (data.getOrNull(baseIndex * 7 + index - 1)?.totalFocusTime()
                                        ?: 0) > 0

                                RoundedCornerShape(
                                    topStart = if (previous) shapes.extraSmall.topStart else shapes.large.topStart,
                                    topEnd = if (next) shapes.extraSmall.topEnd else shapes.large.topEnd,
                                    bottomStart = if (previous) shapes.extraSmall.bottomStart else shapes.large.bottomStart,
                                    bottomEnd = if (next) shapes.extraSmall.bottomEnd else shapes.large.bottomEnd
                                )
                            } else RoundedCornerShape(0)
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(size)
                                .then(
                                    if (background) Modifier.background(
                                        if (currentMonth) colorScheme.primaryContainer
                                        else colorScheme.secondaryContainer,
                                        shape
                                    )
                                    else Modifier
                                )
                        ) {
                            Text(
                                text = it?.date?.dayOfMonth?.toString() ?: "",
                                color =
                                    if (currentMonth) {
                                        if (background) colorScheme.onPrimaryContainer
                                        else colorScheme.onSurface
                                    } else {
                                        if (background) colorScheme.onSecondaryContainer
                                        else colorScheme.outline
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Focus History Calendar")
@Composable
private fun FocusHistoryCalendarPreview() {
    val today1 = LocalDate.now()
    val data = remember {
        List(34) { index ->
            if (index < 3) null
            else {
                val date = today1.minusDays((35 - index).toLong())
                val focusTimeSeconds = (index % 8 + 1) * 60L
                val quarterTime = focusTimeSeconds / 4

                val random = Random.nextInt() % 3

                if (random == 0) Stat(
                    date, 0, 0, 0, 0, 0
                ) else Stat(
                    date = date,
                    focusTimeQ1 = quarterTime,
                    focusTimeQ2 = quarterTime,
                    focusTimeQ3 = quarterTime,
                    focusTimeQ4 = quarterTime,
                    breakTime = focusTimeSeconds / 4
                )
            }
        }
    }

    val averageRankList = listOf(3, 0, 1, 2)

    TomatoTheme(dynamicColor = false) {
        Surface {
            FocusHistoryCalendar(
                data = data,
                averageRankList = averageRankList,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}