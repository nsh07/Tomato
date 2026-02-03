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

package org.nsh07.pomodoro.utils

import androidx.compose.ui.graphics.Color
import java.util.Locale
import java.util.concurrent.TimeUnit

fun millisecondsToStr(t: Long): String {
    require(t >= 0L)
    return String.format(
        Locale.getDefault(),
        "%02d:%02d",
        TimeUnit.MILLISECONDS.toMinutes(t),
        TimeUnit.MILLISECONDS.toSeconds(t) % TimeUnit.MINUTES.toSeconds(1)
    )
}

fun millisecondsToHours(t: Long, format: String = "%dh"): String {
    require(t >= 0L)
    return String.format(
        Locale.getDefault(),
        format,
        TimeUnit.MILLISECONDS.toHours(t)
    )
}

fun millisecondsToMinutes(t: Long, format: String = "%dm"): String {
    require(t >= 0L)
    return String.format(
        Locale.getDefault(),
        format,
        TimeUnit.MILLISECONDS.toMinutes(t)
    )
}

fun millisecondsToHoursMinutes(t: Long, format: String = $$"%1$dh %2$dm"): String {
    require(t >= 0L)
    return String.format(
        Locale.getDefault(),
        format,
        TimeUnit.MILLISECONDS.toHours(t),
        TimeUnit.MILLISECONDS.toMinutes(t) % TimeUnit.HOURS.toMinutes(1)
    )
}

/**
 * Extension function for [String] to convert it to a [androidx.compose.ui.graphics.Color]
 *
 * The base string MUST be of the format produced by [androidx.compose.ui.graphics.Color.toString],
 * i.e, the color black with 100% opacity in sRGB would be represented by:
 *
 *      Color(0.0, 0.0, 0.0, 1.0, sRGB IEC61966-2.1)
 *
 * The behaviour of this function is undefined if the format is not followed
 */
fun String.toColor(): Color {
    // Sample string: Color(0.0, 0.0, 0.0, 1.0, sRGB IEC61966-2.1)
    val comma1 = this.indexOf(',')
    val comma2 = this.indexOf(',', comma1 + 1)
    val comma3 = this.indexOf(',', comma2 + 1)
    val comma4 = this.indexOf(',', comma3 + 1)

    val r = this.substringAfter('(').substringBefore(',').toFloat()
    val g = this.slice(comma1 + 1..<comma2).toFloat()
    val b = this.slice(comma2 + 1..<comma3).toFloat()
    val a = this.slice(comma3 + 1..<comma4).toFloat()
    return Color(r, g, b, a)
}

fun <T> MutableList<T>.onBack() {
    if (size > 1) removeLastOrNull()
}

fun <T> MutableList<T>.onTopLevelNavigate(screen: T) {
    if (size < 2) add(screen)
    else set(1, screen)
}
