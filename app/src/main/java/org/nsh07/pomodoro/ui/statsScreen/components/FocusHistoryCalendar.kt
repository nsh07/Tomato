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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.window.Popup
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.data.Stat
import org.nsh07.pomodoro.ui.theme.TomatoTheme
import org.nsh07.pomodoro.utils.millisecondsToHoursMinutes
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.random.Random

val CALENDAR_CELL_SIZE = 40.dp
val CALENDAR_CELL_HORIZONTAL_GAP = 2.dp
val CALENDAR_CELL_VERTICAL_GAP = 4.dp
val CALENDAR_INTERNAL_PADDING = 20.dp

/**
 * A composable that displays a calendar visualizing focus history.
 *
 * This component shows a calendar grid (days grouped by week) that visualizes the user's focus
 * history. Days with focus time are highlighted. It also distinguishes between days belonging
 * to the last represented month and previous months. The cells are styled with rounded corners
 * to indicate contiguous streaks of focus days.
 *
 * @param data Data to be represented in the heatmap as a [List] of [Stat] objects.. The list is
 * expected to be ordered by date. It is assumed that this list starts with a Monday. Null entries
 * are used for padding the start of the calendar, for example when the actual dates start with a
 * day after Monday.
 * @param averageRankList A list of the ranks of the average focus duration for the 4 parts of a
 * day. See the rankList parameter of [HorizontalStackedBar] for more info. This is used to show a
 * [HorizontalStackedBar] in a tooltip when a date is clicked
 * @param modifier The [Modifier] to be applied to this composable.
 * @param size The size of each calendar cell (width and height).
 * @param horizontalGap The horizontal spacing between calendar cells.
 * @param verticalGap The vertical spacing between calendar rows.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FocusHistoryCalendar(
    data: List<Stat?>,
    averageRankList: List<Int>,
    modifier: Modifier = Modifier,
    size: Dp = CALENDAR_CELL_SIZE,
    horizontalGap: Dp = CALENDAR_CELL_HORIZONTAL_GAP,
    verticalGap: Dp = CALENDAR_CELL_VERTICAL_GAP,
    internalPadding: Dp = CALENDAR_INTERNAL_PADDING
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

    val dateFormat = remember(locale) {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale)
    }

    val tooltipOffset = with(LocalDensity.current) {
        (16 * 2 + // Vertical padding in the tooltip card
                typography.titleSmall.lineHeight.value + 4 + // Heading
                typography.bodyMedium.lineHeight.value + 8 + // Text
                HORIZONTAL_STACKED_BAR_HEIGHT.value + // Obvious
                8).dp.toPx().roundToInt()
    }

    val groupedData = remember(data) {
        data.chunked(7)
    }
    var selectedItemIndex by remember { mutableIntStateOf(-1) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(verticalGap),
        modifier = modifier
            .fillMaxWidth()
            .background(colorScheme.surfaceContainer, shapes.largeIncreased)
            .horizontalScroll(rememberScrollState())
            .padding(internalPadding)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(horizontalGap)) {
            daysOfWeek.fastForEach {
                Text(
                    text = it,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
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
                        val sum = remember(it) { it?.totalFocusTime() ?: 0L }
                        val background = sum > 0
                        val currentMonth =
                            remember(it, last) { it?.date?.month == last?.date?.month }
                        val flatIndex = baseIndex * 7 + index // Calculate flat index

                        val shape = remember(data, background) {
                            if (background) {
                                val next =
                                    (data.getOrNull(flatIndex + 1)?.totalFocusTime() ?: 0) > 0
                                val previous =
                                    (data.getOrNull(flatIndex - 1)?.totalFocusTime() ?: 0) > 0

                                RoundedCornerShape(
                                    topStart = if (previous) shapes.extraSmall.topStart else shapes.large.topStart,
                                    topEnd = if (next) shapes.extraSmall.topEnd else shapes.large.topEnd,
                                    bottomStart = if (previous) shapes.extraSmall.bottomStart else shapes.large.bottomStart,
                                    bottomEnd = if (next) shapes.extraSmall.bottomEnd else shapes.large.bottomEnd
                                )
                            } else RoundedCornerShape(0)
                        }

                        val isTooltipVisible = it != null && selectedItemIndex == flatIndex

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(size)
                                .then(
                                    if (background) Modifier.background(
                                        if (currentMonth) colorScheme.primaryContainer
                                        else colorScheme.secondaryContainer,
                                        if (isTooltipVisible) CircleShape else shape
                                    )
                                    else Modifier
                                )
                                .clickable(enabled = it != null) {
                                    selectedItemIndex = flatIndex
                                }
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

                            if (isTooltipVisible) {
                                val values = remember(it) {
                                    listOf(
                                        it.focusTimeQ1,
                                        it.focusTimeQ2,
                                        it.focusTimeQ3,
                                        it.focusTimeQ4
                                    )
                                }
                                Popup(
                                    alignment = Alignment.TopCenter,
                                    offset = IntOffset(0, -tooltipOffset),
                                    onDismissRequest = { selectedItemIndex = -1 }
                                ) {
                                    Surface(
                                        shape = shapes.large,
                                        color = colorScheme.surfaceContainer,
                                        contentColor = colorScheme.onSurfaceVariant,
                                        shadowElevation = 3.dp,
                                        tonalElevation = 3.dp,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    ) {
                                        Column(Modifier.padding(16.dp)) {
                                            Text(
                                                text = it.date.format(dateFormat),
                                                style = typography.titleSmall
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = millisecondsToHoursMinutes(
                                                    sum,
                                                    stringResource(R.string.hours_and_minutes_format)
                                                ),
                                                style = typography.bodyMedium
                                            )
                                            Spacer(Modifier.height(8.dp))
                                            HorizontalStackedBar(
                                                values = values,
                                                rankList = averageRankList
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