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

@Dao
interface StatDao {
    @Insert(onConflict = REPLACE)
    suspend fun insertStat(stat: Stat)

    @Query("UPDATE stat SET focusTimeQ1 = focusTimeQ1 + :focusTime WHERE date = :date")
    suspend fun addFocusTimeQ1(date: String, focusTime: Long)

    @Query("UPDATE stat SET focusTimeQ2 = focusTimeQ2 + :focusTime WHERE date = :date")
    suspend fun addFocusTimeQ2(date: String, focusTime: Long)

    @Query("UPDATE stat SET focusTimeQ3 = focusTimeQ3 + :focusTime WHERE date = :date")
    suspend fun addFocusTimeQ3(date: String, focusTime: Long)

    @Query("UPDATE stat SET focusTimeQ4 = focusTimeQ4 + :focusTime WHERE date = :date")
    suspend fun addFocusTimeQ4(date: String, focusTime: Long)

    @Query("UPDATE stat SET breakTime = breakTime + :breakTime WHERE date = :date")
    suspend fun addBreakTime(date: String, breakTime: Long)

    @Query("SELECT * FROM stat WHERE date = :date")
    fun getStat(date: String): Flow<Stat?>

    @Query("SELECT * FROM stat")
    fun getStats(): Flow<List<Stat>>

    @Query("SELECT EXISTS (SELECT * FROM stat WHERE date = :date)")
    suspend fun statExists(date: String): Boolean
}