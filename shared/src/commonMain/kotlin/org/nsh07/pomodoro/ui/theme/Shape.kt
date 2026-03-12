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

package org.nsh07.pomodoro.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

object TomatoShapeDefaults {
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    val topListItemShape: RoundedCornerShape
        @Composable get() =
            RoundedCornerShape(
                topStart = shapes.large.topStart,
                topEnd = shapes.large.topEnd,
                bottomStart = shapes.extraSmall.bottomStart,
                bottomEnd = shapes.extraSmall.bottomStart
            )

    val middleListItemShape: RoundedCornerShape
        @Composable get() = RoundedCornerShape(shapes.extraSmall.topStart)

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    val bottomListItemShape: RoundedCornerShape
        @Composable get() =
            RoundedCornerShape(
                topStart = shapes.extraSmall.topStart,
                topEnd = shapes.extraSmall.topEnd,
                bottomStart = shapes.large.bottomStart,
                bottomEnd = shapes.large.bottomEnd
            )

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    val cardShape: CornerBasedShape
        @Composable get() = shapes.large

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun segmentedListItemShapes(
        index: Int,
        count: Int,
        singleElement: Boolean = count == 1
    ): ListItemShapes =
        ListItemDefaults.segmentedShapes(
            index,
            count,
            ListItemDefaults.shapes(
                shape = if (singleElement) shapes.large else shapes.extraSmall,
                selectedShape = CircleShape,
                pressedShape = CircleShape,
                focusedShape = shapes.large,
                hoveredShape = shapes.extraLarge,
                draggedShape = CircleShape
            )
        )

    val PANE_MAX_WIDTH = 600.dp
}