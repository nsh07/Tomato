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
import androidx.compose.ui.graphics.Color

sealed interface SettingsAction {
    data class SaveAlarmEnabled(val enabled: Boolean) : SettingsAction
    data class SaveVibrateEnabled(val enabled: Boolean) : SettingsAction
    data class SaveBlackTheme(val enabled: Boolean) : SettingsAction
    data class SaveAodEnabled(val enabled: Boolean) : SettingsAction
    data class SaveDndEnabled(val enabled: Boolean) : SettingsAction
    data class SaveAlarmSound(val uri: Uri?) : SettingsAction
    data class SaveTheme(val theme: String) : SettingsAction
    data class SaveColorScheme(val color: Color) : SettingsAction
}
