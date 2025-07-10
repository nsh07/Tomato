/*
 * Copyright (c) 2025 Nishant Mishra
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.utils

import java.util.Locale
import kotlin.math.ceil

fun millisecondsToStr(t: Long): String {
    val min = (ceil(t / 1000.0).toInt() / 60)
    val sec = (ceil(t / 1000.0).toInt() % 60)
    return String.format(locale = Locale.getDefault(), "%02d:%02d", min, sec)
}