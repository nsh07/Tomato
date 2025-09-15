/*
 * Copyright (c) 2025 Nishant Mishra
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Interface for reading/writing app preferences to the app's database. This style of storage aims
 * to mimic the Preferences DataStore library, preventing the requirement of migration if new
 * preferences are added
 */
interface PreferenceRepository {
    /**
     * Saves an integer preference key-value pair to the database.
     */
    suspend fun saveIntPreference(key: String, value: Int): Int

    /**
     * Retrieves an integer preference key-value pair from the database.
     */
    suspend fun getIntPreference(key: String): Int?

    /**
     * Erases all integer preference key-value pairs in the database. Do note that the default values
     * will need to be rewritten manually
     */
    suspend fun resetSettings()
}

/**
 * See [PreferenceRepository] for more details
 */
class AppPreferenceRepository(
    private val preferenceDao: PreferenceDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : PreferenceRepository {
    override suspend fun saveIntPreference(key: String, value: Int): Int =
        withContext(ioDispatcher) {
            preferenceDao.insertIntPreference(IntPreference(key, value))
            value
        }

    override suspend fun getIntPreference(key: String): Int? = withContext(ioDispatcher) {
        preferenceDao.getIntPreference(key)
    }

    override suspend fun resetSettings() = withContext(ioDispatcher) {
        preferenceDao.resetIntPreferences()
    }
}