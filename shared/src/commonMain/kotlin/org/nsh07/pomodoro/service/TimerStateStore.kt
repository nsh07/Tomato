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

package org.nsh07.pomodoro.service

/** The part of a session that cannot be derived again once the process is gone. */
data class PersistedTimerState(
    val cycles: Int,
    val startTime: Long,
    val pauseTime: Long,
    val pauseDuration: Long,
    val lastSavedDuration: Long,
    val timerRunning: Boolean,
    val infiniteFocus: Boolean
)

/** Keeps the running session across process death. Both operations must be synchronous. */
interface TimerStateStore {
    /** The stored session, or null if there is none, or it was written before the last reboot. */
    fun load(): PersistedTimerState?

    fun save(state: PersistedTimerState)

    /** For platforms where the timer lives exactly as long as the process does. */
    object None : TimerStateStore {
        override fun load(): PersistedTimerState? = null

        override fun save(state: PersistedTimerState) = Unit
    }
}
