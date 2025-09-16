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
     * Saves a boolean preference key-value pair to the database.
     */
    suspend fun saveBooleanPreference(key: String, value: Boolean): Boolean

    /**
     * Saves a string preference key-value pair to the database.
     */
    suspend fun saveStringPreference(key: String, value: String): String

    /**
     * Retrieves an integer preference key-value pair from the database.
     */
    suspend fun getIntPreference(key: String): Int?

    /**
     * Retrieves a boolean preference key-value pair from the database.
     */
    suspend fun getBooleanPreference(key: String): Boolean?

    /**
     * Retrieves a boolean preference key-value pair as a flow from the database.
     */
    fun getBooleanPreferenceFlow(key: String): Flow<Boolean>

    /**
     * Retrieves a string preference key-value pair from the database.
     */
    suspend fun getStringPreference(key: String): String?

    /**
     * Retrieves a string preference key-value pair as a flow from the database.
     */
    fun getStringPreferenceFlow(key: String): Flow<String>

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

    override suspend fun saveBooleanPreference(key: String, value: Boolean): Boolean =
        withContext(ioDispatcher) {
            preferenceDao.insertBooleanPreference(BooleanPreference(key, value))
            value
        }

    override suspend fun saveStringPreference(key: String, value: String): String =
        withContext(ioDispatcher) {
            preferenceDao.insertStringPreference(StringPreference(key, value))
            value
        }

    override suspend fun getIntPreference(key: String): Int? = withContext(ioDispatcher) {
        preferenceDao.getIntPreference(key)
    }

    override suspend fun getBooleanPreference(key: String): Boolean? = withContext(ioDispatcher) {
        preferenceDao.getBooleanPreference(key)
    }

    override fun getBooleanPreferenceFlow(key: String): Flow<Boolean> =
        preferenceDao.getBooleanPreferenceFlow(key)

    override suspend fun getStringPreference(key: String): String? = withContext(ioDispatcher) {
        preferenceDao.getStringPreference(key)
    }

    override fun getStringPreferenceFlow(key: String): Flow<String> =
        preferenceDao.getStringPreferenceFlow(key)

    override suspend fun resetSettings() = withContext(ioDispatcher) {
        preferenceDao.resetIntPreferences()
        preferenceDao.resetBooleanPreferences()
        preferenceDao.resetStringPreferences()
    }
}