/*
 * Copyright (c) 2025 Nishant Mishra
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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

fun millisecondsToHours(t: Long): String {
    require(t >= 0L)
    return String.format(
        Locale.getDefault(),
        "%dh",
        TimeUnit.MILLISECONDS.toHours(t)
    )
}

fun millisecondsToMinutes(t: Long): String {
    require(t >= 0L)
    return String.format(
        Locale.getDefault(),
        "%dm",
        TimeUnit.MILLISECONDS.toMinutes(t)
    )
}

fun millisecondsToHoursMinutes(t: Long): String {
    require(t >= 0L)
    return String.format(
        Locale.getDefault(),
        "%dh %dm", TimeUnit.MILLISECONDS.toHours(t),
        TimeUnit.MILLISECONDS.toMinutes(t) % TimeUnit.HOURS.toMinutes(1)
    )
}

/**
 * Extension function for [String] to convert it to a [androidx.compose.ui.graphics.Color]
 *
 * The base string must be of the format produced by [androidx.compose.ui.graphics.Color.toString],
 * i.e, the color black with 100% opacity in sRGB would be represented by:
 *
 *      Color(0.0, 0.0, 0.0, 1.0, sRGB IEC61966-2.1)
 */
fun String.toColor(): Color {
    // Sample string: Color(0.0, 0.0, 0.0, 1.0, sRGB IEC61966-2.1)
    val comma1 = this.indexOf(',')
    val comma2 = this.indexOf(',', comma1 + 1)
    val comma3 = this.indexOf(',', comma2 + 1)
    val comma4 = this.indexOf(',', comma3 + 1)

    val r = this.substringAfter('(').substringBefore(',').toFloat()
    val g = this.slice(comma1 + 1..comma2 - 1).toFloat()
    val b = this.slice(comma2 + 1..comma3 - 1).toFloat()
    val a = this.slice(comma3 + 1..comma4 - 1).toFloat()
    return Color(r, g, b, a)
}
