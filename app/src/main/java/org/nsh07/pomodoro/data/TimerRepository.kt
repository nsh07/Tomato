/*
 * Copyright (c) 2025 Nishant Mishra
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.data

/**
 * Interface that holds the timer durations for each timer type. This repository maintains a single
 * source of truth for the timer durations for the various ViewModels in the app.
 */
interface TimerRepository {
    var focusTime: Long
    var shortBreakTime: Long
    var longBreakTime: Long
    var sessionLength: Int
}

/**
 * See [TimerRepository] for more details
 */
class AppTimerRepository : TimerRepository {
    override var focusTime = 25 * 60 * 1000L
    override var shortBreakTime = 5 * 60 * 1000L
    override var longBreakTime = 15 * 60 * 1000L
    override var sessionLength = 4
}