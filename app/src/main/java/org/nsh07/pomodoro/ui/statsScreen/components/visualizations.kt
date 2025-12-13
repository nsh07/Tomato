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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import org.nsh07.pomodoro.ui.theme.TomatoTheme
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
                        .background(colorScheme.primaryContainer)
                        .background(
                            colorScheme.primary.copy(
                                (1f - (rankList.getOrNull(index) ?: 0) * 0.1f).coerceAtLeast(0.1f)
                            )
                        )
                )
            }
        }
    }
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
        Text(
            text = "Not enough data",
            style = typography.bodyLarge,
            color = colorScheme.outline,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview
@Composable
fun HorizontalStackedBarPreview() {
    val values = listOf(38L, 190L, 114L, 14L)
    val rankList = listOf(2, 0, 1, 3)
    TomatoTheme(dynamicColor = false) {
        Surface {
            HorizontalStackedBar(
                values = values,
                rankList = rankList,
                modifier = Modifier.padding(16.dp),
                height = 40.dp,
                gap = 2.dp,
            )
        }
    }
}