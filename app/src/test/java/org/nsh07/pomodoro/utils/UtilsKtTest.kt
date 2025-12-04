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
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test

class UtilsKtTest {

    @Test
    fun `millisecondsToStr zero milliseconds`() {
        assertEquals("00:00", millisecondsToStr(0))
    }

    @Test
    fun `millisecondsToStr positive milliseconds less than a second`() {
        assertEquals("00:00", millisecondsToStr(999))
    }

    @Test
    fun `millisecondsToStr exactly one second`() {
        assertEquals("00:01", millisecondsToStr(1000))
    }

    @Test
    fun `millisecondsToStr positive milliseconds less than a minute`() {
        assertEquals("00:59", millisecondsToStr(59999))
    }

    @Test
    fun `millisecondsToStr exactly one minute`() {
        assertEquals("01:00", millisecondsToStr(60000))
    }

    @Test
    fun `millisecondsToStr positive milliseconds more than a minute`() {
        assertEquals("01:01", millisecondsToStr(61999))
    }

    @Test
    fun `millisecondsToStr large milliseconds value`() {
        assertEquals("99:59", millisecondsToStr(5999999))
    }

    @Test
    fun `millisecondsToStr negative milliseconds`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            millisecondsToStr(-1)
        }
        assertTrue(
            "millisecondsToStr should throw IllegalArgumentException for negative values",
            exception is IllegalArgumentException
        )
    }

    @Test
    fun `millisecondsToStr Long MAX VALUE`() {
        assertEquals("153722867280912:55", millisecondsToStr(Long.MAX_VALUE))
    }

    @Test
    fun `millisecondsToHours zero milliseconds`() {
        assertEquals("0h", millisecondsToHours(0))
    }

    @Test
    fun `millisecondsToHours less than an hour`() {
        assertEquals("0h", millisecondsToHours(3599999))
    }

    @Test
    fun `millisecondsToHours exactly one hour`() {
        assertEquals("1h", millisecondsToHours(3600000))
    }

    @Test
    fun `millisecondsToHours multiple hours`() {
        assertEquals("2h", millisecondsToHours(7200000))
    }

    @Test
    fun `millisecondsToHours large number of hours`() {
        assertEquals("25h", millisecondsToHours(25 * 3600000))
    }

    @Test
    fun `millisecondsToHours negative milliseconds`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            millisecondsToHours(-1)
        }
        assertTrue(
            "millisecondsToHours should throw IllegalArgumentException for negative values",
            exception is IllegalArgumentException
        )
    }

    @Test
    fun `millisecondsToHours Long MAX VALUE`() {
        assertEquals("2562047788015h", millisecondsToHours(Long.MAX_VALUE))
    }

    @Test
    fun `millisecondsToHoursMinutes zero milliseconds`() {
        assertEquals("0h 0m", millisecondsToHoursMinutes(0))
    }

    @Test
    fun `millisecondsToHoursMinutes less than a minute`() {
        assertEquals("0h 0m", millisecondsToHoursMinutes(59999))
    }

    @Test
    fun `millisecondsToHoursMinutes exactly one minute`() {
        assertEquals("0h 1m", millisecondsToHoursMinutes(60000))
    }

    @Test
    fun `millisecondsToHoursMinutes less than an hour but more than a minute`() {
        assertEquals("0h 59m", millisecondsToHoursMinutes(3599999))
    }

    @Test
    fun `millisecondsToHoursMinutes exactly one hour`() {
        assertEquals("1h 0m", millisecondsToHoursMinutes(3600000))
    }

    @Test
    fun `millisecondsToHoursMinutes hours and minutes`() {
        assertEquals("1h 1m", millisecondsToHoursMinutes(3660000))
    }

    @Test
    fun `millisecondsToHoursMinutes multiple hours and minutes`() {
        assertEquals("2h 3m", millisecondsToHoursMinutes(7380000))
    }

    @Test
    fun `millisecondsToHoursMinutes just less than 2 hours`() {
        assertEquals("1h 59m", millisecondsToHoursMinutes(3600000 + 3599999))
    }

    @Test
    fun `millisecondsToHoursMinutes negative milliseconds`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            millisecondsToHoursMinutes(-1)
        }
        assertTrue(
            "millisecondsToHoursMinutes should throw IllegalArgumentException for negative values",
            exception is IllegalArgumentException
        )
    }

    @Test
    fun `millisecondsToHoursMinutes Long MAX VALUE`() {
        assertEquals("2562047788015h 12m", millisecondsToHoursMinutes(Long.MAX_VALUE))
    }

    @Test
    fun `toColor with a standard valid color string`() {
        assertEquals(Color.Black.toString().toColor(), Color.Black)
    }

    @Test
    fun `toColor with color components at maximum valid values`() {
        assertEquals(Color.White.toString().toColor(), Color.White)
    }

    @Test
    fun `toColor with floating point values having multiple decimal places`() {
        assertEquals(
            Color(0.12345f, 0.23456f, 0.34567f, 0.45678f)
                .toString()
                .toColor(),
            Color(0.12345f, 0.23456f, 0.34567f, 0.45678f)
        )
    }
}