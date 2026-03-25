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

package org.nsh07.pomodoro

import org.nsh07.pomodoro.ui.Screen
import org.nsh07.pomodoro.ui.SettingsNavItem
import tomato.shared.generated.resources.Res
import tomato.shared.generated.resources.alarm
import tomato.shared.generated.resources.alarm_sound
import tomato.shared.generated.resources.always_on_display
import tomato.shared.generated.resources.appearance
import tomato.shared.generated.resources.black_theme
import tomato.shared.generated.resources.color_scheme
import tomato.shared.generated.resources.dnd
import tomato.shared.generated.resources.durations
import tomato.shared.generated.resources.media_volume_for_alarm
import tomato.shared.generated.resources.palette
import tomato.shared.generated.resources.sound
import tomato.shared.generated.resources.theme
import tomato.shared.generated.resources.timer
import tomato.shared.generated.resources.timer_filled
import tomato.shared.generated.resources.vibrate

val settingsScreens = listOf(
    SettingsNavItem(
        Screen.Settings.Timer,
        Res.drawable.timer_filled,
        Res.string.timer,
        listOf(Res.string.durations, Res.string.dnd, Res.string.always_on_display)
    ),
    SettingsNavItem(
        Screen.Settings.Alarm,
        Res.drawable.alarm,
        Res.string.alarm,
        listOf(
            Res.string.alarm_sound,
            Res.string.sound,
            Res.string.vibrate,
            Res.string.media_volume_for_alarm
        )
    ),
    SettingsNavItem(
        Screen.Settings.Appearance,
        Res.drawable.palette,
        Res.string.appearance,
        listOf(Res.string.theme, Res.string.color_scheme, Res.string.black_theme)
    )
)
