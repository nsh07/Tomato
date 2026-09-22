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

package org.nsh07.pomodoro.ui.settingsScreen.viewModel

enum class SettingsKey(val key: String) {
    ALARM_ENABLED("alarm_enabled"),
    VIBRATE_ENABLED("vibrate_enabled"),
    BLACK_THEME("black_theme"),
    AOD_ENABLED("aod_enabled"),
    MEDIA_VOLUME_FOR_ALARM("media_volume_for_alarm"),
    SINGLE_PROGRESS_BAR("single_progress_bar"),
    SECURE_AOD("secure_aod"),
    CUSTOM_WINDOW_DECOR("custom_window_decor"),
    ALARM_SOUND("alarm_sound"),
    THEME("theme"),
    COLOR_SCHEME("color_scheme"),
    FOCUS_GOAL("focus_goal"),
    VIBRATION_ON_DURATION("vibration_on_duration"),
    VIBRATION_OFF_DURATION("vibration_off_duration"),
    VIBRATION_AMPLITUDE("vibration_amplitude")
}
