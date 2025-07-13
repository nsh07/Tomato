/*
 * Copyright (c) 2025 Nishant Mishra
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.utils

import java.util.Locale
import java.util.concurrent.TimeUnit

fun millisecondsToStr(t: Long): String =
    String.format(
        Locale.getDefault(),
        "%02d:%02d",
        TimeUnit.MILLISECONDS.toMinutes(t),
        TimeUnit.MILLISECONDS.toSeconds(t) % TimeUnit.MINUTES.toSeconds(1)
    )

fun millisecondsToHours(t: Long): String =
    String.format(
        Locale.getDefault(),
        "%dh",
        TimeUnit.MILLISECONDS.toHours(t)
    )

fun millisecondsToHoursMinutes(t: Long): String =
    String.format(
        Locale.getDefault(),
        "%dh %dm", TimeUnit.MILLISECONDS.toHours(t),
        TimeUnit.MILLISECONDS.toMinutes(t) % TimeUnit.HOURS.toMinutes(1)
    )