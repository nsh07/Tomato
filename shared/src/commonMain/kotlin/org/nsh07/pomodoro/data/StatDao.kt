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
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface StatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStat(stat: Stat)


    @Query("UPDATE stat SET focusTimeQ1 = focusTimeQ1 + :focusTime, updatedAt = :updatedAt WHERE date = :date AND deviceId = :deviceId")
    suspend fun addFocusTimeQ1(date: LocalDate, deviceId: String, focusTime: Long, updatedAt: Long)

    @Query("UPDATE stat SET focusTimeQ2 = focusTimeQ2 + :focusTime, updatedAt = :updatedAt WHERE date = :date AND deviceId = :deviceId")
    suspend fun addFocusTimeQ2(date: LocalDate, deviceId: String, focusTime: Long, updatedAt: Long)

    @Query("UPDATE stat SET focusTimeQ3 = focusTimeQ3 + :focusTime, updatedAt = :updatedAt WHERE date = :date AND deviceId = :deviceId")
    suspend fun addFocusTimeQ3(date: LocalDate, deviceId: String, focusTime: Long, updatedAt: Long)

    @Query("UPDATE stat SET focusTimeQ4 = focusTimeQ4 + :focusTime, updatedAt = :updatedAt WHERE date = :date AND deviceId = :deviceId")
    suspend fun addFocusTimeQ4(date: LocalDate, deviceId: String, focusTime: Long, updatedAt: Long)

    @Query("UPDATE stat SET breakTime = breakTime + :breakTime, updatedAt = :updatedAt WHERE date = :date AND deviceId = :deviceId")
    suspend fun addBreakTime(date: LocalDate, deviceId: String, breakTime: Long, updatedAt: Long)

    @Query(
        """
        SELECT 
            date, 
            'merged' AS deviceId, 
            SUM(focusTimeQ1) AS focusTimeQ1, 
            SUM(focusTimeQ2) AS focusTimeQ2, 
            SUM(focusTimeQ3) AS focusTimeQ3, 
            SUM(focusTimeQ4) AS focusTimeQ4, 
            SUM(breakTime) AS breakTime,
            MAX(updatedAt) AS updatedAt
        FROM stat 
        WHERE date = :date 
        GROUP BY date
    """
    )
    fun getStat(date: LocalDate): Flow<Stat?>

    @Query(
        """
        SELECT 
            date, 
            'merged' AS deviceId, 
            SUM(focusTimeQ1) AS focusTimeQ1, 
            SUM(focusTimeQ2) AS focusTimeQ2, 
            SUM(focusTimeQ3) AS focusTimeQ3, 
            SUM(focusTimeQ4) AS focusTimeQ4, 
            SUM(breakTime) AS breakTime,
            MAX(updatedAt) AS updatedAt
        FROM stat 
        GROUP BY date 
        ORDER BY date DESC 
        LIMIT :n
    """
    )
    fun getLastNDaysStats(n: Int): Flow<List<Stat>>

    @Query("SELECT * FROM stat")
    suspend fun getAllRows(): List<Stat>

    @Query(
        """
        SELECT
            CAST(AVG(focusTimeQ1) AS INTEGER) AS focusTimeQ1,
            CAST(AVG(focusTimeQ2) AS INTEGER) AS focusTimeQ2,
            CAST(AVG(focusTimeQ3) AS INTEGER) AS focusTimeQ3,
            CAST(AVG(focusTimeQ4) AS INTEGER) AS focusTimeQ4,
            CAST(AVG(breakTime) AS INTEGER) AS breakTime 
        FROM (
            SELECT 
                SUM(focusTimeQ1) AS focusTimeQ1,
                SUM(focusTimeQ2) AS focusTimeQ2,
                SUM(focusTimeQ3) AS focusTimeQ3,
                SUM(focusTimeQ4) AS focusTimeQ4,
                SUM(breakTime) AS breakTime
            FROM stat
            GROUP BY date
            ORDER BY date DESC
            LIMIT :n
        )
        WHERE focusTimeQ1 > 0 OR focusTimeQ2 > 0 OR focusTimeQ3 > 0 OR focusTimeQ4 > 0
    """
    )
    fun getLastNDaysAvgStats(n: Int): Flow<StatTime?>

    @Query("SELECT EXISTS (SELECT 1 FROM stat WHERE date = :date)")
    suspend fun statExists(date: LocalDate): Boolean

    @Query("SELECT date FROM stat ORDER BY date DESC LIMIT 1")
    suspend fun getLastDate(): LocalDate?

    @Query("SELECT SUM(focusTimeQ1 + focusTimeQ2 + focusTimeQ3 + focusTimeQ4) FROM stat")
    fun getAllTimeTotalFocusTime(): Flow<Long?>

    @Query("DELETE FROM stat")
    suspend fun clearAll()

    @Query("SELECT * FROM stat WHERE date = :date AND deviceId = :deviceId")
    suspend fun getStatByDateAndDevice(date: LocalDate, deviceId: String): Stat?

    @Transaction
    suspend fun insertStatsIfNewer(stats: List<Stat>) {
        stats.forEach { stat ->
            val existing = getStatByDateAndDevice(stat.date, stat.deviceId)
            if (existing == null || stat.updatedAt > existing.updatedAt) {
                insertStat(stat)
            }
        }
    }
}