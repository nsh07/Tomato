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

package org.nsh07.pomodoro.ui

import org.nsh07.pomodoro.R
import tomato.shared.generated.resources.Res
import tomato.shared.generated.resources.alarm
import tomato.shared.generated.resources.palette
import tomato.shared.generated.resources.timer_filled

val settingsScreens = listOf(
    SettingsNavItem(
        Screen.Settings.Timer,
        Res.drawable.timer_filled,
        R.string.timer,
        listOf(R.string.durations, R.string.dnd, R.string.always_on_display)
    ),
    SettingsNavItem(
        Screen.Settings.Alarm,
        Res.drawable.alarm,
        R.string.alarm,
        listOf(
            R.string.alarm_sound,
            R.string.sound,
            R.string.vibrate,
            R.string.media_volume_for_alarm
        )
    ),
    SettingsNavItem(
        Screen.Settings.Appearance,
        Res.drawable.palette,
        R.string.appearance,
        listOf(R.string.theme, R.string.color_scheme, R.string.black_theme)
    )
)
