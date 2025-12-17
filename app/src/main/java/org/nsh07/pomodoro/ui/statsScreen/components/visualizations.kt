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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMaxBy
import androidx.compose.ui.window.Popup
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.data.Stat
import org.nsh07.pomodoro.ui.theme.TomatoTheme
import org.nsh07.pomodoro.utils.millisecondsToHoursMinutes
import org.nsh07.pomodoro.utils.millisecondsToMinutes
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

val HORIZONTAL_STACKED_BAR_HEIGHT = 40.dp

/**
 * A "Horizontal stacked bar" component, which can be considered as a horizontal stacked bar chart
 * with a single bar. This component can be stacked in a column to create a "100% stacked bar chart"
 * where each bar is the same length to easily visualize proportions of each type of value
 * represented
 *
 * @param values Values to be represented by the bar
 * @param rankList A list of the rank of each element if the list was sorted in a non-increasing
 * order
 * @param height Height of the bar
 * @param gap Gap between each part of the bar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorizontalStackedBar(
    values: List<Long>,
    modifier: Modifier = Modifier,
    rankList: List<Int> = remember(values) {
        val sortedIndices = values.indices.sortedByDescending { values[it] }
        val ranks = MutableList(values.size) { 0 }

        sortedIndices.forEachIndexed { rank, originalIndex ->
            ranks[originalIndex] = rank
        }

        ranks
    },
    labelFormatter: @Composable (Int, Long, Long) -> String = { index, value, total ->
        buildString {
            append(
                when (index) {
                    0 -> "[00:00 - 06:00] "
                    1 -> "[06:00 - 12:00] "
                    2 -> "[12:00 - 18:00] "
                    else -> "[18:00 - 24:00] "
                }
            )
            if (value < 60 * 60 * 1000)
                append(
                    millisecondsToMinutes(
                        value,
                        stringResource(R.string.minutes_format)
                    )
                )
            else
                append(
                    millisecondsToHoursMinutes(
                        value,
                        stringResource(R.string.hours_and_minutes_format)
                    )
                )
            append(" (%.2f".format((value.toFloat() / total) * 100) + "%)")
        }
    },
    height: Dp = HORIZONTAL_STACKED_BAR_HEIGHT,
    gap: Dp = 2.dp
) {
    val shapes = shapes
    val firstNonZeroIndex = remember(values) { values.indexOfFirst { it > 0L } }
    val lastNonZeroIndex = remember(values) { values.indexOfLast { it > 0L } }

    val tooltipOffset = with(LocalDensity.current) { (24 + 4).dp.toPx().roundToInt() }

    if (firstNonZeroIndex != -1)
        Row(
            horizontalArrangement = Arrangement.spacedBy(gap),
            modifier = modifier.height(height)
        ) {
            values.fastForEachIndexed { index, item ->
                if (item > 0L) {
                    var showTooltip by remember { mutableStateOf(false) }
                    val shape = remember(index, firstNonZeroIndex, lastNonZeroIndex) {
                        if (firstNonZeroIndex == lastNonZeroIndex) shapes.large
                        else when (index) {
                            firstNonZeroIndex -> shapes.large.copy(
                                topEnd = shapes.extraSmall.topEnd,
                                bottomEnd = shapes.extraSmall.bottomEnd
                            )

                            lastNonZeroIndex -> shapes.large.copy(
                                topStart = shapes.extraSmall.topStart,
                                bottomStart = shapes.extraSmall.bottomStart
                            )

                            else -> shapes.extraSmall
                        }
                    }
                    Box(
                        Modifier
                            .weight(item.toFloat())
                            .height(height)
                            .clip(shape)
                            .background(colorScheme.surfaceVariant)
                            .background(
                                colorScheme.primary.copy(
                                    (1f - (rankList.getOrNull(index) ?: 0) * 0.1f).coerceAtLeast(
                                        0.1f
                                    )
                                )
                            )
                            .clickable { showTooltip = true }
                    ) {
                        if (showTooltip) {
                            Popup(
                                alignment = Alignment.TopCenter,
                                offset = IntOffset(0, -tooltipOffset),
                                onDismissRequest = {
                                    showTooltip = false
                                }
                            ) {
                                Text(
                                    text = labelFormatter(index, item, values.sum()),
                                    style = typography.bodySmall,
                                    color = colorScheme.inverseOnSurface,
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp)
                                        .background(
                                            color = colorScheme.inverseSurface,
                                            shape = shapes.extraSmall
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    else
        Spacer(
            modifier
                .fillMaxWidth()
                .height(height)
                .clip(shapes.large)
                .background(colorScheme.surfaceVariant)
        )
}

@Composable
fun FocusBreakRatioVisualization(
    focusDuration: Long,
    breakDuration: Long,
    modifier: Modifier = Modifier,
    height: Dp = HORIZONTAL_STACKED_BAR_HEIGHT,
    gap: Dp = 2.dp
) {
    if (focusDuration + breakDuration > 0) {
        val shapes = shapes
        val focusPercentage = ((focusDuration / (focusDuration.toFloat() + breakDuration)) * 100)
        val breakPercentage = 100 - focusPercentage

        val focusShape = remember(breakDuration) {
            if (breakDuration > 0) shapes.large.copy(
                topEnd = shapes.extraSmall.topEnd,
                bottomEnd = shapes.extraSmall.bottomEnd
            ) else shapes.large
        }
        val breakShape = remember(focusDuration) {
            if (focusDuration > 0) shapes.large.copy(
                topStart = shapes.extraSmall.topStart,
                bottomStart = shapes.extraSmall.bottomStart
            ) else shapes.large
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(gap),
            modifier = modifier
        ) {
            Text(
                text = "${focusPercentage.roundToInt()}%",
                style = typography.bodyLarge,
                color = colorScheme.primary,
                modifier = Modifier.padding(end = 6.dp)
            )
            if (focusDuration > 0) Spacer(
                Modifier
                    .weight(focusPercentage)
                    .height(height)
                    .background(
                        colorScheme.primary,
                        focusShape
                    )
            )
            if (breakDuration > 0) Spacer(
                Modifier
                    .weight(breakPercentage)
                    .height(height)
                    .background(
                        colorScheme.tertiary,
                        breakShape
                    )
            )
            Text(
                text = breakPercentage.roundToInt().toString() + '%',
                style = typography.bodyLarge,
                color = colorScheme.tertiary,
                modifier = Modifier.padding(start = 6.dp)
            )
        }
    } else {
        Spacer(
            modifier
                .fillMaxWidth()
                .height(height)
                .clip(shapes.large)
                .background(colorScheme.surfaceVariant)
        )
    }
}

val HEATMAP_CELL_SIZE = 28.dp
val HEATMAP_CELL_GAP = 2.dp

/**
 * A horizontally scrollable heatmap with persistent week labels in the first column
 *
 * @param data Data to be represented in the heatmap as a [List] of [Stat] objects. A null value
 * passed in the list can be used to insert gaps in the heatmap, and can be used to, for example,
 * delimit months by inserting a null week. Note that it is assumed that the dates are continuous
 * (without gaps) and start with a Monday.
 * @param averageRankList A list of the ranks of the average focus duration for the 4 parts of a
 * day. See the rankList parameter of [HorizontalStackedBar] for more info. This is used to show a
 * [HorizontalStackedBar] in a tooltip when a cell is clicked
 * @param modifier Modifier to be applied to the heatmap
 * @param maxValue Maximum total focus duration of the items present in [data]. This value must
 * correspond to the total focus duration one of the elements in [data] for accurate representation.
 */
@Composable
fun HeatmapWithWeekLabels(
    data: List<Stat?>,
    averageRankList: List<Int>,
    modifier: Modifier = Modifier,
    size: Dp = HEATMAP_CELL_SIZE,
    gap: Dp = HEATMAP_CELL_GAP,
    contentPadding: PaddingValues = PaddingValues(),
    maxValue: Long = remember {
        data.fastMaxBy { it?.totalFocusTime() ?: 0 }?.totalFocusTime() ?: 0
    },
) {
    val locale = Locale.getDefault()
    val shapes = shapes

    val daysOfWeek = remember(locale) {
        DayOfWeek.entries.map {
            it.getDisplayName(
                TextStyle.NARROW,
                locale
            )
        }
    } // Names of the 7 days of the week in the current locale

    val tooltipOffset = with(LocalDensity.current) {
        (16 * 2 + // Vertical padding in the tooltip card
                typography.titleSmall.lineHeight.value + 4 + // Heading
                typography.bodyMedium.lineHeight.value + 8 + // Text
                HORIZONTAL_STACKED_BAR_HEIGHT.value + // Obvious
                8).dp.toPx().roundToInt()
    }

    val dateFormat = remember(locale) {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale)
    }

    var activeTooltipIndex by remember { mutableIntStateOf(-1) }

    Row(modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(gap),
        ) {
            daysOfWeek.fastForEach {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(size)
                ) {
                    Text(
                        text = it,
                        style = typography.labelSmall
                    )
                }
            }
        }
        LazyHorizontalGrid(
            rows = GridCells.Fixed(7),
            modifier = Modifier
                .height(size * 7 + gap * 6)
                .clip(shapes.small.copy(topEnd = CornerSize(0), bottomEnd = CornerSize(0))),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(gap),
            horizontalArrangement = Arrangement.spacedBy(gap)
        ) {
            itemsIndexed(
                items = data,
                key = { index, it -> it?.date?.toEpochDay() ?: index.toString() },
                contentType = { _, it -> if (it == null) "spacer" else "cell" }
            ) { index, it ->
                if (it == null) {
                    Spacer(Modifier.size(size))
                } else {
                    val sum = remember { it.totalFocusTime() }

                    val shape = remember(data, index) {
                        val top = data.getOrNull(index - 1) != null && index % 7 != 0
                        val end = data.getOrNull(index + 7) != null
                        val bottom = data.getOrNull(index + 1) != null && index % 7 != 6
                        val start = data.getOrNull(index - 7) != null

                        RoundedCornerShape(
                            topStart = if (top || start) shapes.extraSmall.topStart else shapes.small.topStart,
                            topEnd = if (top || end) shapes.extraSmall.topEnd else shapes.small.topEnd,
                            bottomStart = if (bottom || start) shapes.extraSmall.bottomStart else shapes.small.bottomStart,
                            bottomEnd = if (bottom || end) shapes.extraSmall.bottomEnd else shapes.small.bottomEnd
                        )
                    }

                    val isTooltipVisible = activeTooltipIndex == index

                    Box(
                        Modifier
                            .size(size)
                            .background(
                                if (sum > 0)
                                    colorScheme.primary.copy(0.4f + (0.6f * sum / maxValue))
                                else colorScheme.surfaceVariant,
                                if (!isTooltipVisible) shape else CircleShape
                            )
                            .clickable { activeTooltipIndex = index }
                    ) {
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
                                onDismissRequest = {
                                    activeTooltipIndex = -1
                                }
                            ) {
                                ElevatedCard(
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = colorScheme.surfaceContainer,
                                        contentColor = colorScheme.onSurfaceVariant
                                    ),
                                    shape = shapes.large,
                                    elevation = CardDefaults.elevatedCardElevation(3.dp),
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                ) {
                                    Column(Modifier.padding(16.dp)) {
                                        Text(
                                            text = it.date.format(dateFormat),
                                            style = typography.titleSmall
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = millisecondsToHoursMinutes(sum),
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

@Preview
@Composable
fun HorizontalStackedBarPreview() {
    val values = listOf(
        listOf(38L, 190L, 114L, 14L),
        listOf(0L, 0L, 0L, 0L)
    )
    val rankList = listOf(2, 0, 1, 3)
    TomatoTheme(dynamicColor = false) {
        Surface {
            Column {
                values.fastForEach {
                    HorizontalStackedBar(
                        values = it,
                        rankList = rankList,
                        modifier = Modifier.padding(16.dp),
                        height = HORIZONTAL_STACKED_BAR_HEIGHT,
                        gap = 2.dp,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun HeatmapWithWeekLabelsPreview() {
    val startDate = LocalDate.of(2024, 1, 1) // Monday
    val sampleData = remember {
        buildList {
            (0..93).forEach { index ->
                val date = startDate.plusDays(index.toLong())
                val focusStat = Stat(date, index % 10L / 2, 0, 0, 0, 0) // Varying focus durations

                if (date.month != date.minusDays(1).month && index > 0)
                    repeat(7) { add(null) }

                add(focusStat)
            }
        }
    }
    TomatoTheme(dynamicColor = false) {
        Surface {
            HeatmapWithWeekLabels(
                data = sampleData,
                averageRankList = listOf(3, 0, 1, 2),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .height(HEATMAP_CELL_SIZE * 7 + HEATMAP_CELL_GAP * 6)
            )
        }
    }
}