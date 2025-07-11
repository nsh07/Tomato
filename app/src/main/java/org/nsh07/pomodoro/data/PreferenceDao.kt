package org.nsh07.pomodoro.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PreferenceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIntPreference(preference: IntPreference)

    @Query("DELETE FROM int_preference")
    suspend fun resetIntPreferences()

    @Query("SELECT value FROM int_preference WHERE `key` = :key")
    suspend fun getIntPreference(key: String): Int?
}