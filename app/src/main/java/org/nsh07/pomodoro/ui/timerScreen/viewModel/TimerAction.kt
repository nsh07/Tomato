/*
 * Copyright (c) 2025 Nishant Mishra
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.ui.timerScreen.viewModel

sealed interface TimerAction {
    data class SkipTimer(val fromButton: Boolean) : TimerAction

    data object ResetTimer : TimerAction
    data object StopAlarm : TimerAction
    data object ToggleTimer : TimerAction
}