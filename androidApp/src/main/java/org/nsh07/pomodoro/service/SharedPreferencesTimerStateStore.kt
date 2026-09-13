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

import android.content.Context
import android.os.SystemClock
import androidx.core.content.edit
import kotlin.math.abs

/** Shared preferences impl of [TimerStateStore] for Android */
class SharedPreferencesTimerStateStore(context: Context) : TimerStateStore {

    private val prefs = context.getSharedPreferences("timer_session", Context.MODE_PRIVATE)

    /** See [TimerStateStore.load] */
    override fun load(): PersistedTimerState? {
        val bootInstant = prefs.getLong(KEY_BOOT_INSTANT, NO_SESSION)
        if (bootInstant == NO_SESSION) return null
        if (abs(bootInstant - bootInstant()) > BOOT_INSTANT_TOLERANCE) return null

        return PersistedTimerState(
            cycles = prefs.getInt(KEY_CYCLES, 0),
            startTime = prefs.getLong(KEY_START_TIME, 0L),
            pauseTime = prefs.getLong(KEY_PAUSE_TIME, 0L),
            pauseDuration = prefs.getLong(KEY_PAUSE_DURATION, 0L),
            lastSavedDuration = prefs.getLong(KEY_LAST_SAVED_DURATION, 0L),
            timerRunning = prefs.getBoolean(KEY_TIMER_RUNNING, false),
            infiniteFocus = prefs.getBoolean(KEY_INFINITE_FOCUS, false)
        )
    }

    override fun save(state: PersistedTimerState) {
        prefs.edit(commit = true) {
            putLong(KEY_BOOT_INSTANT, bootInstant())
            putInt(KEY_CYCLES, state.cycles)
            putLong(KEY_START_TIME, state.startTime)
            putLong(KEY_PAUSE_TIME, state.pauseTime)
            putLong(KEY_PAUSE_DURATION, state.pauseDuration)
            putLong(KEY_LAST_SAVED_DURATION, state.lastSavedDuration)
            putBoolean(KEY_TIMER_RUNNING, state.timerRunning)
            putBoolean(KEY_INFINITE_FOCUS, state.infiniteFocus)
        }
    }

    /** Wall clock instant the device booted at, give or take clock adjustments */
    private fun bootInstant() = System.currentTimeMillis() - SystemClock.elapsedRealtime()

    private companion object {
        const val KEY_BOOT_INSTANT = "boot_instant"
        const val KEY_CYCLES = "cycles"
        const val KEY_START_TIME = "start_time"
        const val KEY_PAUSE_TIME = "pause_time"
        const val KEY_PAUSE_DURATION = "pause_duration"
        const val KEY_LAST_SAVED_DURATION = "last_saved_duration"
        const val KEY_TIMER_RUNNING = "timer_running"
        const val KEY_INFINITE_FOCUS = "infinite_focus"

        const val NO_SESSION = Long.MIN_VALUE

        /** Leaves room for the clock being nudged by the network */
        const val BOOT_INSTANT_TOLERANCE = 60_000L
    }
}
