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

package org.nsh07.pomodoro.ui.settingsScreen.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.MutableStyleState
import androidx.compose.foundation.style.animate
import androidx.compose.foundation.style.size
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.nsh07.pomodoro.ui.settingsScreen.screens.selected

@OptIn(ExperimentalFoundationStyleApi::class)
@Composable
fun TopicShapeIcon(
    shape: Shape,
    containerColor: Color,
    shapeColor: Color,
    modifier: Modifier = Modifier,
    selectedContainerColor: Color = containerColor,
    selectedShapeColor: Color = shapeColor,
    styleState: MutableStyleState = remember { MutableStyleState(null) },
    size: Dp = 72.dp,
    shapeSize: Dp = 40.dp
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.styleable(styleState) {
            size(size)
            shape(CircleShape)
            background(containerColor)
            selected { animate { background(selectedContainerColor) } }
        }
    ) {
        Box(
            Modifier.styleable(styleState) {
                size(shapeSize)
                shape(shape)
                background(shapeColor)
                selected { animate { background(selectedShapeColor) } }
            }
        )
    }
}
