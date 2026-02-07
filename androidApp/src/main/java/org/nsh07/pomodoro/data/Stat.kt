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

package org.nsh07.pomodoro.data

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Data class for storing the user's statistics in the app's database. This class stores the focus
 * durations for the 4 quarters of a day (00:00 - 12:00, 12:00 - 16:00, 16:00 - 20:00, 20:00 - 00:00)
 * separately for later analysis (e.g. for showing which parts of the day are most productive).
 */
@Immutable
@Entity(tableName = "stat")
data class Stat(
    @PrimaryKey
    val date: LocalDate,
    val focusTimeQ1: Long,
    val focusTimeQ2: Long,
    val focusTimeQ3: Long,
    val focusTimeQ4: Long,
    val breakTime: Long
) {
    fun totalFocusTime() = focusTimeQ1 + focusTimeQ2 + focusTimeQ3 + focusTimeQ4
}

data class StatTime(
    val focusTimeQ1: Long,
    val focusTimeQ2: Long,
    val focusTimeQ3: Long,
    val focusTimeQ4: Long,
    val breakTime: Long,
)