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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.window.Popup
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.ui.theme.TomatoTheme
import org.nsh07.pomodoro.utils.millisecondsToHoursMinutes
import org.nsh07.pomodoro.utils.millisecondsToMinutes
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
                text = "${breakPercentage.roundToInt()}%",
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
