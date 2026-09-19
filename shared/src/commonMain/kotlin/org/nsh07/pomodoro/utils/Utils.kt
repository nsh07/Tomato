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

package org.nsh07.pomodoro.utils

import java.util.Locale
import java.util.concurrent.TimeUnit

fun millisecondsToStr(t: Long): String {
    return String.format(
        Locale.getDefault(),
        "%02d:%02d",
        TimeUnit.MILLISECONDS.toMinutes(t),
        TimeUnit.MILLISECONDS.toSeconds(t) % TimeUnit.MINUTES.toSeconds(1)
    )
}

fun millisecondsToHours(t: Long, format: String = "%dh"): String {
    return String.format(
        Locale.getDefault(),
        format,
        TimeUnit.MILLISECONDS.toHours(t)
    )
}

fun millisecondsToMinutes(t: Long, format: String = "%dm"): String {
    return String.format(
        Locale.getDefault(),
        format,
        TimeUnit.MILLISECONDS.toMinutes(t)
    )
}

fun millisecondsToHoursMinutes(t: Long, format: String = $$"%1$dh %2$dm"): String {
    return String.format(
        Locale.getDefault(),
        format,
        TimeUnit.MILLISECONDS.toHours(t),
        TimeUnit.MILLISECONDS.toMinutes(t) % TimeUnit.HOURS.toMinutes(1)
    )
}

fun <T> MutableList<T>.onBack() {
    if (size > 1) removeLastOrNull()
}

fun <T> MutableList<T>.onTopLevelNavigate(screen: T) {
    if (size < 2) add(screen)
    else set(1, screen)
}

/**
 * Checks the system SDK version on Android
 *
 * @param version SDK version code
 * @return false if device is not running Android or SDK version is lower than [version], else true
 */
expect fun androidSdkVersionAtLeast(version: Int): Boolean

expect fun androidDeviceManufacturerIs(manufacturer: String): Boolean

/**
 * Returns the default alarm tone for the device
 *
 * @return string representation of the path (or URI) of the alarm tone, or null if none
 */
expect fun getDefaultAlarmTone(): String?

/**
 * Cross-platform function for using the system-provided logger
 *
 * @param tag tag for the log message. This is often used on Android to mark logs.
 * @param message message to be logged
 */
expect fun logError(tag: String, message: String): Int

enum class OS {
    ANDROID, LINUX, WINDOWS, MACOS
}

expect val currentOS: OS