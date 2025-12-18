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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.data.Stat
import org.nsh07.pomodoro.utils.millisecondsToHoursMinutes
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun FocusBreakdownTooltip(
    item: Stat,
    rankList: List<Int>,
    dateFormat: DateTimeFormatter,
    onDismissRequest: (() -> Unit)
) {
    val tooltipOffset = with(LocalDensity.current) {
        (16 * 2 + // Vertical padding in the tooltip card
                typography.titleSmall.lineHeight.value + 4 + // Heading
                typography.bodyMedium.lineHeight.value + 8 + // Text
                HORIZONTAL_STACKED_BAR_HEIGHT.value + // Obvious
                8).dp.toPx().roundToInt()
    }
    val values = listOf(
        item.focusTimeQ1,
        item.focusTimeQ2,
        item.focusTimeQ3,
        item.focusTimeQ4
    )
    Popup(
        alignment = Alignment.TopCenter,
        offset = IntOffset(0, -tooltipOffset),
        onDismissRequest = onDismissRequest
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
                    text = item.date.format(dateFormat),
                    style = typography.titleSmall
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = millisecondsToHoursMinutes(
                        item.totalFocusTime(),
                        stringResource(R.string.hours_and_minutes_format)
                    ),
                    style = typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                HorizontalStackedBar(
                    values = values,
                    rankList = rankList
                )
            }
        }
    }
}