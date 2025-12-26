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

package org.nsh07.pomodoro.widget.componenets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme.colors
import androidx.glance.LocalContext
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.width

val HORIZONTAL_STACKED_BAR_HEIGHT = 40.dp

/**
 * A version of [org.nsh07.pomodoro.ui.statsScreen.components.HorizontalStackedBar] that uses Glance
 * composables for use in widgets.
 */
@Composable
fun HorizontalStackedBarGlance(
    values: List<Long>,
    width: Dp,
    modifier: GlanceModifier = GlanceModifier,
    rankList: List<Int> = remember(values) {
        val sortedIndices = values.indices.sortedByDescending { values[it] }
        val ranks = MutableList(values.size) { 0 }

        sortedIndices.forEachIndexed { rank, originalIndex ->
            ranks[originalIndex] = rank
        }

        ranks
    },
    height: Dp = HORIZONTAL_STACKED_BAR_HEIGHT,
    gap: Dp = 2.dp
) {
    val firstNonZeroIndex = remember(values) { values.indexOfFirst { it > 0L } }
    val nonZeroItems = remember(values) { values.count { it > 0 } }
    val effectiveWidth = width - ((nonZeroItems - 1).coerceAtLeast(0) * gap.value).dp
    val totalSum = remember(values) { values.sum() }

    val context = LocalContext.current

    if (firstNonZeroIndex != -1)
        Row(
            modifier = modifier
                .height(height)
                .cornerRadius(16.dp)
        ) {
            values.fastForEachIndexed { index, item ->
                if (item > 0L) {
                    Box(
                        GlanceModifier
                            .width((effectiveWidth.value * (item.toFloat() / totalSum)).dp)
                            .height(height)
                            .cornerRadius(4.dp)
                            .background(
                                colors.primary.getColor(context).copy(
                                    (1f - (rankList.getOrNull(index) ?: 0) * 0.1f).coerceAtLeast(
                                        0.1f
                                    )
                                ).compositeOver(colors.surfaceVariant.getColor(context))
                            )
                    ) {}
                    Spacer(GlanceModifier.width(gap))
                }
            }
        }
    else
        Box(
            modifier
                .fillMaxWidth()
                .height(height)
                .cornerRadius(16.dp)
                .background(colors.surfaceVariant)
        ) {}
}