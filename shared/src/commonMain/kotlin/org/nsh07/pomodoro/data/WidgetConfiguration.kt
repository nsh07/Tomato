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

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "widget_configuration")
data class WidgetConfiguration(
    @PrimaryKey
    val appWidgetId: Int,
    val opacity: Float,
    val backgroundRole: String,
    val foregroundRole: String,
    val headerRole: String,
    /** Background color role for the skip / restart button group (timer widget only). */
    @ColumnInfo(defaultValue = "tertiary")
    val skipButtonRole: String = "tertiary",
    /** Foreground / icon color role for the skip / restart button group (timer widget only). */
    @ColumnInfo(defaultValue = "onTertiary")
    val onSkipButtonRole: String = "onTertiary",
    /** Corner radius in dp for the bars in the history widget. */
    @ColumnInfo(defaultValue = "16")
    val barCornerRadius: Int = 16
)
