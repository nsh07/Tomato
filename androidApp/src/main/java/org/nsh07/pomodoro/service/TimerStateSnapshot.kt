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

package org.nsh07.pomodoro.service

import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerState

data class TimerStateSnapshot(
    var lastSavedDuration: Long = 0L,
    var time: Long,
    var cycles: Int = 0,
    var startTime: Long = 0L,
    var pauseTime: Long = 0L,
    var pauseDuration: Long = 0L,
    var timerState: TimerState
) {
    fun save(
        lastSavedDuration: Long,
        time: Long,
        cycles: Int,
        startTime: Long,
        pauseTime: Long,
        pauseDuration: Long,
        timerState: TimerState
    ) {
        this.lastSavedDuration = lastSavedDuration
        this.time = time
        this.cycles = cycles
        this.startTime = startTime
        this.pauseTime = pauseTime
        this.pauseDuration = pauseDuration
        this.timerState = timerState
    }
}
