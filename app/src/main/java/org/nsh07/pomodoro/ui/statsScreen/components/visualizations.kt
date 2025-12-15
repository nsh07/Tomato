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
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMaxBy
import org.nsh07.pomodoro.ui.theme.TomatoTheme
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

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
    height: Dp = 40.dp,
    gap: Dp = 2.dp
) {
    val firstNonZeroIndex = remember(values) { values.indexOfFirst { it > 0L } }
    val lastNonZeroIndex = remember(values) { values.indexOfLast { it > 0L } }

    if (firstNonZeroIndex != -1)
        Row(
            horizontalArrangement = Arrangement.spacedBy(gap),
            modifier = modifier.height(height)
        ) {
            values.fastForEachIndexed { index, item ->
                if (item > 0L) {
                    val shape =
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
                    Spacer(
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
                    )
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
    height: Dp = 40.dp,
    gap: Dp = 2.dp
) {
    if (focusDuration + breakDuration > 0) {
        val focusPercentage = ((focusDuration / (focusDuration.toFloat() + breakDuration)) * 100)
        val breakPercentage = 100 - focusPercentage
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(gap),
            modifier = modifier
        ) {
            Text(
                text = focusPercentage.roundToInt().toString() + '%',
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
                        if (breakDuration > 0) shapes.large.copy(
                            topEnd = shapes.extraSmall.topEnd,
                            bottomEnd = shapes.extraSmall.bottomEnd
                        ) else shapes.large
                    )
            )
            if (breakDuration > 0) Spacer(
                Modifier
                    .weight(breakPercentage)
                    .height(height)
                    .background(
                        colorScheme.tertiary,
                        if (focusDuration > 0) shapes.large.copy(
                            topStart = shapes.extraSmall.topStart,
                            bottomStart = shapes.extraSmall.bottomStart
                        ) else shapes.large
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
 * A horizontally scrollable heatmap with week labels in the first column
 *
 * @param data Data to be represented in the heatmap in the form of [Pair]s of [LocalDate]s and
 * their corresponding focus durations as a list. A null value passed in the list can be used to
 * insert gaps in the heatmap, and can be used to, for example, delimit months by inserting an
 * empty week
 * @param modifier Modifier to be applied to the heatmap
 * @param maxValue Maximum total value of the items present in [data]. This value must correspond to
 * the sum of the list present in one of the elements on [data] for accurate representation.
 *
 * Note that it is assumed that the dates are continuous (without gaps) and start with a Monday
 */
@Composable
fun HeatmapWithWeekLabels(
    data: List<List<Long>?>,
    modifier: Modifier = Modifier,
    size: Dp = HEATMAP_CELL_SIZE,
    gap: Dp = HEATMAP_CELL_GAP,
    contentPadding: PaddingValues = PaddingValues(),
    maxValue: Long = remember { data.fastMaxBy { it?.sum() ?: 0 }?.sum() ?: 0 },
) {
    val locale = Locale.getDefault()
    val shapes = shapes

    val first7 = remember(locale) {
        val monday = LocalDate.of(2024, 1, 1) // Monday

        buildList {
            repeat(7) {
                add(
                    monday
                        .plusDays(it.toLong())
                        .dayOfWeek
                        .getDisplayName(
                            TextStyle.NARROW,
                            locale
                        )
                )
            }
        }
    } // Names of the 7 days of the week in the current locale

    Row(modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(gap),
        ) {
            first7.fastForEach {
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
            itemsIndexed(data) { index, it ->
                if (it == null) {
                    Spacer(Modifier.size(size))
                } else {
                    val sum = remember { it.sum().toFloat() }

                    val shape = remember {
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

                    if (sum > 0)
                        Spacer(
                            Modifier
                                .size(size)
                                .background(
                                    colorScheme.primary.copy(0.33f + (0.67f * sum / maxValue)),
                                    shape
                                )
                        )
                    else Spacer(
                        Modifier
                            .size(size)
                            .background(colorScheme.surfaceVariant, shape)
                    )
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
                        height = 40.dp,
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
                val focusDurations = listOf(index % 10L / 2) // Varying focus durations

                if (date.month != date.minusDays(1).month && index > 0)
                    repeat(7) { add(null) }

                add(focusDurations)
            }
        }
    }
    TomatoTheme(dynamicColor = false) {
        Surface {
            HeatmapWithWeekLabels(
                data = sampleData,
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .height(HEATMAP_CELL_SIZE * 7 + HEATMAP_CELL_GAP * 6)
            )
        }
    }
}