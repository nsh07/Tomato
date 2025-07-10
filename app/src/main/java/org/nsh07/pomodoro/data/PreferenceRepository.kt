package org.nsh07.pomodoro.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface PreferenceRepository {
    suspend fun saveIntPreference(key: String, value: Int): Int

    suspend fun getIntPreference(key: String): Int?

    suspend fun resetSettings()
}

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