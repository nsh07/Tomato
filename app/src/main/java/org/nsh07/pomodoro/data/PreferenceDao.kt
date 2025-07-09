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

@Dao
interface PreferenceDao {
    @Insert(onConflict = REPLACE)
    suspend fun insertIntPreference(preference: IntPreference)

    @Query("DELETE FROM int_preference")
    suspend fun resetIntPreferences()

    @Query("SELECT value FROM int_preference WHERE `key` = :key")
    suspend fun getIntPreference(key: String): Int?
}