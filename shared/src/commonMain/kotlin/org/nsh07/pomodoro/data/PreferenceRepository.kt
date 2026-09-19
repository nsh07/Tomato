/*
 * Copyright (c) 2025-2026 Nishant Mishra
 *
 * This file is part of Tomato - a minimalist pomodoro timer for Android.
 *
 * Tomato is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Tomato is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Tomato.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.data

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineDispatcher
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
     * Saves a long preference key-value pair to the database. Shares its storage with
     * [saveIntPreference].
     */
    suspend fun saveLongPreference(key: String, value: Long): Long

    /**
     * Saves a boolean preference key-value pair to the database.
     */
    suspend fun saveBooleanPreference(key: String, value: Boolean): Boolean

    /**
     * Saves a string preference key-value pair to the database.
     */
    suspend fun saveStringPreference(key: String, value: String): String

    suspend fun saveColorPreference(key: String, value: Color): Color

    /**
     * Retrieves an integer preference key-value pair from the database.
     */
    suspend fun getIntPreference(key: String): Int?

    /**
     * Retrieves a long preference key-value pair from the database.
     */
    suspend fun getLongPreference(key: String): Long?

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

    suspend fun getColorPreference(key: String): Color?

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
    private val ioDispatcher: CoroutineDispatcher
) : PreferenceRepository {
    override suspend fun saveIntPreference(key: String, value: Int): Int =
        withContext(ioDispatcher) {
            preferenceDao.insertIntPreference(IntPreference(key, value.toLong()))
            value
        }

    override suspend fun saveLongPreference(key: String, value: Long): Long =
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

    override suspend fun saveColorPreference(key: String, value: Color): Color =
        withContext(ioDispatcher) {
            preferenceDao.insertIntPreference(
                IntPreference(
                    key,
                    ComposeColorConverter.fromColor(value)
                )
            )
            value
        }

    override suspend fun getIntPreference(key: String): Int? = withContext(ioDispatcher) {
        preferenceDao.getIntPreference(key)?.toInt()
    }

    override suspend fun getLongPreference(key: String): Long? = withContext(ioDispatcher) {
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

    override suspend fun getColorPreference(key: String): Color? = withContext(ioDispatcher) {
        preferenceDao.getIntPreference(key)?.let { ComposeColorConverter.toColor(it) }
    }

    override fun getStringPreferenceFlow(key: String): Flow<String> =
        preferenceDao.getStringPreferenceFlow(key)

    override suspend fun resetSettings() = withContext(ioDispatcher) {
        preferenceDao.resetIntPreferences()
        preferenceDao.resetBooleanPreferences()
        preferenceDao.resetStringPreferences()
    }
}