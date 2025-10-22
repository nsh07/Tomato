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

package org.nsh07.pomodoro.ui

import org.nsh07.pomodoro.R

val mainScreens = listOf(
    NavItem(
        Screen.Timer,
        R.drawable.timer_outlined,
        R.drawable.timer_filled,
        R.string.timer
    ),
    NavItem(
        Screen.Stats,
        R.drawable.monitoring,
        R.drawable.monitoring_filled,
        R.string.stats
    ),
    NavItem(
        Screen.Settings.Main,
        R.drawable.settings,
        R.drawable.settings_filled,
        R.string.settings
    )
)

val settingsScreens = listOf(
    SettingsNavItem(
        Screen.Settings.Timer,
        R.drawable.timer_filled,
        R.string.timer,
        listOf(R.string.durations, R.string.session_length, R.string.always_on_display)
    ),
    SettingsNavItem(
        Screen.Settings.Alarm,
        R.drawable.alarm,
        R.string.alarm,
        listOf(R.string.alarm_sound, R.string.alarm, R.string.vibrate)
    ),
    SettingsNavItem(
        Screen.Settings.Appearance,
        R.drawable.palette,
        R.string.appearance,
        listOf(R.string.color_scheme, R.string.theme, R.string.black_theme)
    )
)
