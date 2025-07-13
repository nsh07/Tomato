/*
 * Copyright (c) 2025 Nishant Mishra
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Data class for storing the user's statistics in the app's database. This class stores the focus
 * durations for the 4 quarters of a day (00:00 - 12:00, 12:00 - 16:00, 16:00 - 20:00, 20:00 - 00:00)
 * separately for later analysis (e.g. for showing which parts of the day are most productive).
 */
@Entity(tableName = "stat")
data class Stat(
    @PrimaryKey
    val date: LocalDate,
    val focusTimeQ1: Long,
    val focusTimeQ2: Long,
    val focusTimeQ3: Long,
    val focusTimeQ4: Long,
    val breakTime: Long
) {
    fun totalFocusTime() = focusTimeQ1 + focusTimeQ2 + focusTimeQ3 + focusTimeQ4
}

data class StatSummary(
    val date: LocalDate,
    val focusTime: Long,
    val breakTime: Long
)

data class StatFocusTime(
    val focusTimeQ1: Long,
    val focusTimeQ2: Long,
    val focusTimeQ3: Long,
    val focusTimeQ4: Long
) {
    fun total() = focusTimeQ1 + focusTimeQ2 + focusTimeQ3 + focusTimeQ4
}