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

package org.nsh07.pomodoro.widget

import android.content.Context
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.GlanceTheme.colors
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import kotlinx.coroutines.flow.first
import org.nsh07.pomodoro.MainActivity
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.data.AppStatRepository
import org.nsh07.pomodoro.data.Stat
import org.nsh07.pomodoro.utils.millisecondsToHoursMinutes
import java.time.LocalDate

class TodayAppWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            DpSize(109.dp, 56.dp),  // 2x1
            DpSize(109.dp, 115.dp), // 2x2
            DpSize(245.dp, 56.dp),  // 4x1
            DpSize(245.dp, 115.dp)  // 4x2
        )
    )

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        val statRepository = AppStatRepository.get(context)

        val stat = statRepository.getTodayStat().first()
            ?: Stat(LocalDate.now(), 0, 0, 0, 0, 0)

        provideContent {
            GlanceTheme {
                Content(stat)
            }
        }
    }

    @Composable
    private fun Content(stat: Stat) {
        val context = LocalContext.current
        val size = LocalSize.current
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(colors.secondaryContainer)
                .padding(16.dp)
                .clickable(actionStartActivity<MainActivity>())
        ) {
            Text(
                context.getString(R.string.focus),
                style = TextStyle(
                    color = colors.onPrimaryContainer,
                    fontSize = typography.titleMedium.fontSize
                )
            )
            Text(
                millisecondsToHoursMinutes(
                    stat.totalFocusTime(),
                    context.getString(R.string.hours_and_minutes_format)
                ),
                style = TextStyle(
                    color = colors.onPrimaryContainer,
                    fontSize = typography.displaySmall.fontSize,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )
        }
    }
}
