/*
 * Copyright (c) 2025 Nishant Mishra
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
import java.time.LocalDate

@Dao
interface StatDao {
    @Insert(onConflict = REPLACE)
    suspend fun insertStat(stat: Stat)

    @Query("UPDATE stat SET focusTimeQ1 = focusTimeQ1 + :focusTime WHERE date = :date")
    suspend fun addFocusTimeQ1(date: LocalDate, focusTime: Long)

    @Query("UPDATE stat SET focusTimeQ2 = focusTimeQ2 + :focusTime WHERE date = :date")
    suspend fun addFocusTimeQ2(date: LocalDate, focusTime: Long)

    @Query("UPDATE stat SET focusTimeQ3 = focusTimeQ3 + :focusTime WHERE date = :date")
    suspend fun addFocusTimeQ3(date: LocalDate, focusTime: Long)

    @Query("UPDATE stat SET focusTimeQ4 = focusTimeQ4 + :focusTime WHERE date = :date")
    suspend fun addFocusTimeQ4(date: LocalDate, focusTime: Long)

    @Query("UPDATE stat SET breakTime = breakTime + :breakTime WHERE date = :date")
    suspend fun addBreakTime(date: LocalDate, breakTime: Long)

    @Query("SELECT * FROM stat WHERE date = :date")
    fun getStat(date: LocalDate): Flow<Stat?>

    @Query("SELECT date, focusTimeQ1, focusTimeQ2, focusTimeQ3, focusTimeQ4, breakTime FROM stat ORDER BY date DESC LIMIT :n")
    fun getLastNDaysStats(n: Int): Flow<List<Stat>>

    @Query(
        "SELECT " +
                "AVG(focusTimeQ1) AS focusTimeQ1, " +
                "AVG(focusTimeQ2) AS focusTimeQ2, " +
                "AVG(focusTimeQ3) AS focusTimeQ3, " +
                "AVG(focusTimeQ4) AS focusTimeQ4, " +
                "AVG(breakTime) AS breakTime " +
                "FROM (" +
                "SELECT * FROM (" +
                "SELECT focusTimeQ1, focusTimeQ2, focusTimeQ3, focusTimeQ4, breakTime FROM stat ORDER BY date DESC LIMIT :n" +
                ") " +
                "WHERE focusTimeQ1 != 0 OR focusTimeQ2 != 0 OR focusTimeQ3 != 0 OR focusTimeQ4 != 0 " +
                ")"
    )
    fun getLastNDaysAvgStats(n: Int): Flow<StatTime?>

    @Query("SELECT EXISTS (SELECT * FROM stat WHERE date = :date)")
    suspend fun statExists(date: LocalDate): Boolean

    @Query("SELECT date FROM stat ORDER BY date DESC LIMIT 1")
    suspend fun getLastDate(): LocalDate?
}