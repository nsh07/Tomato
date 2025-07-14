/*
 * Copyright (c) 2025 Nishant Mishra
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.utils

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

}