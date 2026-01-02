/*
 * Copyright (c) 2026 Nishant Mishra
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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.nsh07.pomodoro.ui.theme.CustomColors.listItemColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SliderListItem(
    sliderState: SliderState,
    label: String,
    trailingLabel: String,
    shape: CornerBasedShape,
    modifier: Modifier = Modifier,
    containerColor: Color = listItemColors.containerColor,
    icon: @Composable () -> Unit,
) {
    Column(modifier.background(containerColor, shape)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            Text(label, style = typography.bodyLarge)
            Spacer(Modifier.weight(1f))
            Text(
                trailingLabel,
                style = typography.labelMedium,
                color = colorScheme.onSurfaceVariant
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp)
        ) {
            Slider(
                sliderState,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(20.dp))
            icon()
        }
    }
}