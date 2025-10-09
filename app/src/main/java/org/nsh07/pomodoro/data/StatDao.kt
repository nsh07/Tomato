/*
 * Copyright (c) 2025 Nishant Mishra
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface StatDao {
    @Insert(onConflict = REPLACE)
    suspend fun insertStat(stat: Stat)

    @Query("UPDATE stat SET focusTimeQ1 = focusTimeQ1 + :focusTime WHERE date = :date")
    suspend fun addFocusTimeQ1(date: LocalDate, focusTime: Long)

    @Query("UPDATE stat SET focusTimeQ2 = focusTimeQ2 + :focusTime WHERE date = :date")
    suspend fun addFocusTimeQ2(date: LocalDate, focusTime: Long)

    @Query("UPDATE stat SET focusTimeQ3 = focusTimeQ3 + :focusTime WHERE date = :date")
    suspend fun addFocusTimeQ3(date: LocalDate, focusTime: Long)

    @Query("UPDATE stat SET focusTimeQ4 = focusTimeQ4 + :focusTime WHERE date = :date")
    suspend fun addFocusTimeQ4(date: LocalDate, focusTime: Long)

    @Query("UPDATE stat SET breakTime = breakTime + :breakTime WHERE date = :date")
    suspend fun addBreakTime(date: LocalDate, breakTime: Long)

    @Query("SELECT * FROM stat WHERE date = :date")
    fun getStat(date: LocalDate): Flow<Stat?>

    @Query("SELECT date, focusTimeQ1 + focusTimeQ2 + focusTimeQ3 + focusTimeQ4 as focusTime, breakTime FROM stat ORDER BY date DESC LIMIT :n")
    fun getLastNDaysStatsSummary(n: Int): Flow<List<StatSummary>>

    @Query(
        "SELECT " +
                "AVG(focusTimeQ1) AS focusTimeQ1, " +
                "AVG(focusTimeQ2) AS focusTimeQ2, " +
                "AVG(focusTimeQ3) AS focusTimeQ3, " +
                "AVG(focusTimeQ4) AS focusTimeQ4 " +
                "FROM (" +
                "SELECT * FROM (" +
                "SELECT focusTimeQ1, focusTimeQ2, focusTimeQ3, focusTimeQ4 FROM stat ORDER BY date DESC LIMIT :n" +
                ") " +
                "WHERE focusTimeQ1 != 0 OR focusTimeQ2 != 0 OR focusTimeQ3 != 0 OR focusTimeQ4 != 0 " +
                ")"
    )
    fun getLastNDaysAvgFocusTimes(n: Int): Flow<StatFocusTime?>

    @Query("SELECT EXISTS (SELECT * FROM stat WHERE date = :date)")
    suspend fun statExists(date: LocalDate): Boolean

    @Query("SELECT date FROM stat ORDER BY date DESC LIMIT 1")
    suspend fun getLastDate(): LocalDate?
}