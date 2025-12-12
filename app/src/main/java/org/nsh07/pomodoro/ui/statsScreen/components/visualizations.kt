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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import org.nsh07.pomodoro.ui.theme.TomatoTheme
import kotlin.math.roundToInt

/**
 * A custom implementation of the 1-Dimensional heatmap plot that varies the width of the cells
 * instead of the colors. The colors are varied according to the `maxIndex` value passed but they do
 * NOT correspond to the actual values represented by the cells, and exist for aesthetic reasons
 * only.
 */
@Composable
fun VariableWidth1DHeatmap(
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
        modifier = modifier
    ) {
        values.fastForEachIndexed { index, item ->
            if (item > 0L) {
                val shape = when (index) {
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
    val focusPercentage = ((focusDuration / (focusDuration.toFloat() + breakDuration)) * 100)
    val breakPercentage = ((breakDuration / (focusDuration.toFloat() + breakDuration)) * 100)
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
        Spacer(
            Modifier
                .weight(focusPercentage)
                .height(height)
                .background(
                    colorScheme.primary,
                    shapes.large.copy(
                        topEnd = shapes.extraSmall.topEnd,
                        bottomEnd = shapes.extraSmall.bottomEnd
                    )
                )
        )
        Spacer(
            Modifier
                .weight(breakPercentage)
                .height(height)
                .background(
                    colorScheme.tertiary,
                    shapes.large.copy(
                        topStart = shapes.extraSmall.topStart,
                        bottomStart = shapes.extraSmall.bottomStart
                    )
                )
        )
        Text(
            text = breakPercentage.roundToInt().toString() + '%',
            style = typography.bodyLarge,
            color = colorScheme.tertiary,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}

@Preview
@Composable
fun VariableWidth1DHeatmapPreview() {
    val values = listOf(38L, 190L, 114L, 14L)
    val rankList = listOf(2, 0, 1, 3)
    TomatoTheme(dynamicColor = false) {
        Surface {
            VariableWidth1DHeatmap(
                values = values,
                rankList = rankList,
                modifier = Modifier.padding(16.dp),
                height = 40.dp,
                gap = 2.dp,
            )
        }
    }
}