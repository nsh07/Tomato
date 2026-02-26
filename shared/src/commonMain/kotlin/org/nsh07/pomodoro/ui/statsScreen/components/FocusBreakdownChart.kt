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

package org.nsh07.pomodoro.ui.statsScreen.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter

@Composable
fun FocusBreakdownChart(
    modelProducer: CartesianChartModelProducer,
    hoursFormat: String,
    hoursMinutesFormat: String,
    minutesFormat: String,
    modifier: Modifier = Modifier
) {
    TimeColumnChart(
        modelProducer,
        hoursFormat = hoursFormat,
        hoursMinutesFormat = hoursMinutesFormat,
        minutesFormat = minutesFormat,
        xValueFormatter = CartesianValueFormatter { _, value, _ ->
            when (value) {
                0.0 -> "0 - 6"
                1.0 -> "6 - 12"
                2.0 -> "12 - 18"
                3.0 -> "18 - 24"
                else -> ""
            }
        },
        goal = 0,
        modifier = modifier
    )
}