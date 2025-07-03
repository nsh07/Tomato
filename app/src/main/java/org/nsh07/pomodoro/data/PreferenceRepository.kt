package org.nsh07.pomodoro.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

interface PreferencesRepository {
    suspend fun saveIntPreference(key: String, value: Int)

    fun getIntPreference(key: String): Flow<Int?>

    suspend fun resetSettings()
}

class AppPreferenceRepository(
    private val preferenceDao: PreferenceDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
): PreferencesRepository {
    override suspend fun saveIntPreference(key: String, value: Int) =
        withContext(ioDispatcher) {
            preferenceDao.insertIntPreference(IntPreference(key, value))
        }

    override fun getIntPreference(key: String): Flow<Int?> =
        preferenceDao.getIntPreference(key)

    override suspend fun resetSettings() = withContext(ioDispatcher) {
        preferenceDao.resetIntPreferences()
    }
}