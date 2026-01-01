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

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime

/**
 * Interface for reading/writing statistics to the app's database. Ideally, writing should be done
 * through the timer screen's ViewModel and reading should be done through the stats screen's
 * ViewModel
 */
interface StatRepository {
    suspend fun insertStat(stat: Stat)

    suspend fun addFocusTime(focusTime: Long)

    suspend fun addBreakTime(breakTime: Long)

    fun getTodayStat(): Flow<Stat?>

    fun getLastNDaysStats(n: Int): Flow<List<Stat>>

    fun getLastNDaysAverageFocusTimes(n: Int): Flow<StatTime?>

    fun getAllTimeTotalFocusTime(): Flow<Long?>

    suspend fun getLastDate(): LocalDate?

    suspend fun deleteAllStats()
}

/**
 * See [StatRepository] for more details
 */
class AppStatRepository(
    private val statDao: StatDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : StatRepository {
    override suspend fun insertStat(stat: Stat) = statDao.insertStat(stat)

    override suspend fun addFocusTime(focusTime: Long) = withContext(ioDispatcher) {
        val currentDate = LocalDate.now()
        val currentTime = LocalTime.now().toSecondOfDay()
        val secondsInDay = 24 * 60 * 60

        if (statDao.statExists(currentDate)) {
            when (currentTime) {
                in 0..(secondsInDay / 4) ->
                    statDao.addFocusTimeQ1(currentDate, focusTime)

                in (secondsInDay / 4)..(secondsInDay / 2) ->
                    statDao.addFocusTimeQ2(currentDate, focusTime)

                in (secondsInDay / 2)..(3 * secondsInDay / 4) ->
                    statDao.addFocusTimeQ3(currentDate, focusTime)

                else -> statDao.addFocusTimeQ4(currentDate, focusTime)
            }
        } else {
            when (currentTime) {
                in 0..(secondsInDay / 4) ->
                    statDao.insertStat(
                        Stat(currentDate, focusTime, 0, 0, 0, 0)
                    )

                in (secondsInDay / 4)..(secondsInDay / 2) ->
                    statDao.insertStat(
                        Stat(currentDate, 0, focusTime, 0, 0, 0)
                    )

                in (secondsInDay / 2)..(3 * secondsInDay / 4) ->
                    statDao.insertStat(
                        Stat(currentDate, 0, 0, focusTime, 0, 0)
                    )

                else ->
                    statDao.insertStat(
                        Stat(currentDate, 0, 0, 0, focusTime, 0)
                    )
            }
        }
    }

    override suspend fun addBreakTime(breakTime: Long) = withContext(ioDispatcher) {
        val currentDate = LocalDate.now()
        if (statDao.statExists(currentDate)) {
            statDao.addBreakTime(currentDate, breakTime)
        } else {
            statDao.insertStat(Stat(currentDate, 0, 0, 0, 0, breakTime))
        }
    }

    override fun getTodayStat(): Flow<Stat?> {
        val currentDate = LocalDate.now()
        return statDao.getStat(currentDate)
    }

    override fun getLastNDaysStats(n: Int): Flow<List<Stat>> =
        statDao.getLastNDaysStats(n)

    override fun getLastNDaysAverageFocusTimes(n: Int): Flow<StatTime?> =
        statDao.getLastNDaysAvgStats(n)

    override fun getAllTimeTotalFocusTime(): Flow<Long?> =
        statDao.getAllTimeTotalFocusTime()

    override suspend fun getLastDate(): LocalDate? = statDao.getLastDate()

    override suspend fun deleteAllStats() = statDao.clearAll()

    companion object {
        fun get(context: Context) =
            AppStatRepository(AppDatabase.getDatabase(context).statDao())
    }
}