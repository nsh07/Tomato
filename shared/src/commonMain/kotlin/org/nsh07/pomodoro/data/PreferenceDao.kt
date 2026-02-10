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

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PreferenceDao {
    @Insert(onConflict = REPLACE)
    suspend fun insertIntPreference(preference: IntPreference)

    @Insert(onConflict = REPLACE)
    suspend fun insertBooleanPreference(preference: BooleanPreference)

    @Insert(onConflict = REPLACE)
    suspend fun insertStringPreference(preference: StringPreference)

    @Query("DELETE FROM int_preference")
    suspend fun resetIntPreferences()

    @Query("DELETE FROM boolean_preference")
    suspend fun resetBooleanPreferences()

    @Query("DELETE FROM string_preference")
    suspend fun resetStringPreferences()

    @Query("SELECT value FROM int_preference WHERE `key` = :key")
    suspend fun getIntPreference(key: String): Int?

    @Query("SELECT value FROM boolean_preference WHERE `key` = :key")
    suspend fun getBooleanPreference(key: String): Boolean?

    @Query("SELECT value FROM boolean_preference WHERE `key` = :key")
    fun getBooleanPreferenceFlow(key: String): Flow<Boolean>

    @Query("SELECT value FROM string_preference WHERE `key` = :key")
    suspend fun getStringPreference(key: String): String?

    @Query("SELECT value FROM string_preference WHERE `key` = :key")
    fun getStringPreferenceFlow(key: String): Flow<String>
}