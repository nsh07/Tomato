/*
 * Copyright (c) 2025 Nishant Mishra
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.ui.timerScreen.viewModel

data class TimerState(
    val timerMode: TimerMode = TimerMode.FOCUS,
    val timeStr: String = "25:00",
    val totalTime: Long = 25 * 60,
    val timerRunning: Boolean = false,
    val nextTimerMode: TimerMode = TimerMode.SHORT_BREAK,
    val nextTimeStr: String = "5:00",
    val showBrandTitle: Boolean = true,
    val currentFocusCount: Int = 1,
    val totalFocusCount: Int = 4,
    val alarmRinging: Boolean = false
)

enum class TimerMode {
    FOCUS, SHORT_BREAK, LONG_BREAK, BRAND
}