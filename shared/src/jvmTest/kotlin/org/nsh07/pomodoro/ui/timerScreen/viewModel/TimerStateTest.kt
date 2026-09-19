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

package org.nsh07.pomodoro.ui.timerScreen.viewModel

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TimerStateTest {

    private val reset = TimerState(timeStr = "25:00", totalTime = 25 * MINUTE)

    @Test
    fun `a timer at its reset state has no session`() {
        assertFalse(reset.sessionActive)
    }

    @Test
    fun `a running timer is a session`() {
        assertTrue(reset.copy(timerRunning = true).sessionActive)
    }

    @Test
    fun `a first interval paused part way is a session`() {
        assertTrue(reset.copy(timeStr = "24:59").sessionActive)
    }

    @Test
    fun `a later interval is a session even when untouched`() {
        assertTrue(
            reset.copy(
                timerMode = TimerMode.SHORT_BREAK,
                timeStr = "05:00",
                totalTime = 5 * MINUTE
            ).sessionActive
        )
        assertTrue(reset.copy(currentFocusCount = 2).sessionActive)
    }

    @Test
    fun `an untouched infinite focus has no session`() {
        val infinite =
            reset.copy(infiniteFocus = true, timeStr = "00:00", totalTime = Long.MAX_VALUE)
        assertFalse(infinite.sessionActive)
        assertTrue(infinite.copy(timeStr = "00:05").sessionActive)
    }

    private companion object {
        const val MINUTE = 60_000L
    }
}
