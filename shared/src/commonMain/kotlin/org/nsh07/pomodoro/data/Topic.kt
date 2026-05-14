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

package org.nsh07.pomodoro.data

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a Topic with its own timer settings.
 * If a setting is null, it inherits from the global settings.
 */
@Entity(tableName = "topic")
data class Topic(
    @PrimaryKey
    val id: String, // name in lowercase, spaces replaced with underscores
    val name: String,
    val color: Color,
    val focusTime: Long?,
    val shortBreakTime: Long?,
    val longBreakTime: Long?,
    val sessionLength: Int?,
    val autoStartNextSession: Boolean?,
    val dndEnabled: Boolean?
)