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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Density
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.parent
import io.github.vinceglb.filekit.path
import org.koin.compose.koinInject
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsAction

@Composable
actual fun AodSystemBarsHandler(
    density: Density,
    windowInfo: WindowInfo,
    secureAod: Boolean,
    setTimerFrequency: (Float) -> Unit
) {
    val windowState: WindowState = koinInject()

    DisposableEffect(Unit) {
        setTimerFrequency(1f)
        windowState.placement = WindowPlacement.Fullscreen

        onDispose {
            setTimerFrequency(60f)
            windowState.placement = WindowPlacement.Floating
        }
    }
}

actual fun Modifier.androidSystemGestureExclusion(): Modifier = this

// TODO: use a working implementation
actual fun htmlToAnnotatedString(html: String): AnnotatedString =
    AnnotatedString(html.replace("</?([a-z]+)>".toRegex(), ""))

@Composable
actual fun rememberRequestDndPermissionCallback(): (Boolean) -> Unit = {}

@Composable
actual fun rememberRequestNotificationPermissionCallback(): () -> Unit = {}

@Composable
actual fun rememberRingtonePickerLauncherCallback(
    alarmSoundFilePath: String?,
    onResult: (SettingsAction) -> Unit
): suspend () -> Unit = {
    // TODO: copy the file to the data directory and use its path instead, to avoid dependence on a file that the user may delete
    val file = FileKit.openFilePicker(
        directory = alarmSoundFilePath?.let {
            PlatformFile(it).parent()
        }
    )
    onResult(SettingsAction.SaveAlarmSound(file?.path))
}

@Composable
actual fun rememberRingtoneNameProviderCallback(): suspend (String?) -> String = { path ->
    path?.let { PlatformFile(it).name } ?: "..."
}