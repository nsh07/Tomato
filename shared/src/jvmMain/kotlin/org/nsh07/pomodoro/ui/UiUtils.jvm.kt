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

package org.nsh07.pomodoro.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Density
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsAction

@Composable
actual fun AodSystemBarsHandler(
    density: Density,
    windowInfo: WindowInfo,
    secureAod: Boolean,
    setTimerFrequency: (Float) -> Unit
) {
}

actual fun Modifier.androidSystemGestureExclusion(): Modifier = this

// TODO: use a working implementation
actual fun htmlToAnnotatedString(html: String): AnnotatedString =
    AnnotatedString(html.replace("</?([a-z]+)>".toRegex(), ""))

@Composable
actual fun rememberRequestDndPermissionCallback(): (Boolean) -> Unit {
    // TODO: implement
    return {}
}

@Composable
actual fun rememberRequestNotificationPermissionCallback(): () -> Unit {
    // TODO: implement
    return {}
}

@Composable
actual fun rememberRingtonePickerLauncherCallback(
    alarmSoundFilePath: String?,
    onResult: (SettingsAction) -> Unit
): () -> Unit {
    // TODO: implement
    return {}
}

@Composable
actual fun rememberRingtoneNameProviderCallback(): suspend (String?) -> String {
    // TODO: implement
    return { _ -> "..." }
}