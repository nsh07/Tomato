/*
 * Copyright (c) 2025 Nishant Mishra
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.LocalDate

interface StatRepository {
    suspend fun addFocusTime(focusTime: Int)

    suspend fun addBreakTime(breakTime: Int)

    fun getTodayStat(): Flow<Stat?>

    fun getAllStats(): Flow<List<Stat>>
}

class AppStatRepository(
    private val statDao: StatDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : StatRepository {
    override suspend fun addFocusTime(focusTime: Int) = withContext(ioDispatcher) {
        val currentDate = LocalDate.now().toString()
        if (statDao.statExists(currentDate)) {
            statDao.addFocusTime(currentDate, focusTime)
        } else {
            statDao.insertStat(Stat(currentDate, focusTime, 0))
        }
    }

    override suspend fun addBreakTime(breakTime: Int) = withContext(ioDispatcher) {
        val currentDate = LocalDate.now().toString()
        if (statDao.statExists(currentDate)) {
            statDao.addBreakTime(currentDate, breakTime)
        } else {
            statDao.insertStat(Stat(currentDate, 0, breakTime))
        }
    }

    override fun getTodayStat(): Flow<Stat?> {
        val currentDate = LocalDate.now().toString()
        return statDao.getStat(currentDate)
    }

    override fun getAllStats(): Flow<List<Stat>> = statDao.getStats()
}