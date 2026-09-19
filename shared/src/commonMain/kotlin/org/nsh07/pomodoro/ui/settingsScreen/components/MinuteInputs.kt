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

package org.nsh07.pomodoro.ui.settingsScreen.components

import androidx.compose.runtime.Immutable
import org.nsh07.pomodoro.data.Topic

/** The minutes typed into a topic's duration fields, which need not be valid durations yet */
@Immutable
data class MinuteInputs(
    val focus: String,
    val shortBreak: String,
    val longBreak: String
) {
    constructor(topic: Topic) : this(
        focus = topic.focusTime.toMinutes(),
        shortBreak = topic.shortBreakTime.toMinutes(),
        longBreak = topic.longBreakTime.toMinutes()
    )
}

fun Long.toMinutes(): String = (this / (60 * 1000)).toString()

/** Whether [this] is something that can be typed into a duration field of [maxDigits] digits */
fun CharSequence.isMinutesInput(maxDigits: Int): Boolean =
    length <= maxDigits && all { it.isDigit() }

fun CharSequence.isValidMinutesInput(): Boolean = (toString().toLongOrNull() ?: 0L) > 0L

/** [this] as a duration in milliseconds, or null if it is not a valid one */
fun CharSequence.minutesToMillisOrNull(): Long? =
    if (isValidMinutesInput()) toString().toLong() * 60 * 1000 else null
