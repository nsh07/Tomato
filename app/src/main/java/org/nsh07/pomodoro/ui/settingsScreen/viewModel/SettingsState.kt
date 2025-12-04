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

package org.nsh07.pomodoro.ui.settingsScreen.viewModel

import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class SettingsState(
    val theme: String = "auto",
    val colorScheme: String = Color.White.toString(),
    val blackTheme: Boolean = false,
    val aodEnabled: Boolean = false,
    val alarmEnabled: Boolean = true,
    val vibrateEnabled: Boolean = true,
    val dndEnabled: Boolean = false,
    val mediaVolumeForAlarm: Boolean = false,
    val singleProgressBar: Boolean = false,

    val focusTime: Long = 25 * 60 * 1000L,
    val shortBreakTime: Long = 5 * 60 * 1000L,
    val longBreakTime: Long = 15 * 60 * 1000L,

    val sessionLength: Int = 4,

    val alarmSoundUri: Uri? =
        Settings.System.DEFAULT_ALARM_ALERT_URI ?: Settings.System.DEFAULT_RINGTONE_URI
)
