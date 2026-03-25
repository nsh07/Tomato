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

package org.nsh07.pomodoro.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Class for storing boolean preferences in the app's database
 */
@Entity(tableName = "boolean_preference")
data class BooleanPreference(
    @PrimaryKey
    val key: String,
    val value: Boolean
)

/**
 * Class for storing integer preferences in the app's database
 */
@Entity(tableName = "int_preference")
data class IntPreference(
    @PrimaryKey
    val key: String,
    val value: Int
)

/**
 * Class for storing string preferences in the app's database
 */
@Entity(tableName = "string_preference")
data class StringPreference(
    @PrimaryKey
    val key: String,
    val value: String
)