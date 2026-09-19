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

package org.nsh07.pomodoro.utils

import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.ChecksSdkIntAtLeast

@ChecksSdkIntAtLeast(parameter = 0)
actual fun androidSdkVersionAtLeast(version: Int): Boolean =
    Build.VERSION.SDK_INT >= version

actual fun getDefaultAlarmTone(): String? =
    (Settings.System.DEFAULT_ALARM_ALERT_URI
        ?: Settings.System.DEFAULT_RINGTONE_URI)?.toString()

actual fun logError(tag: String, message: String): Int =
    Log.e(tag, message)

actual fun androidDeviceManufacturerIs(manufacturer: String): Boolean =
    Build.MANUFACTURER == manufacturer

actual val currentOS: OS = OS.ANDROID