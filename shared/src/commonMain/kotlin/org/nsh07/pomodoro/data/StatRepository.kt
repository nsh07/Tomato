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

import kotlinx.coroutines.CoroutineDispatcher
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

    suspend fun replaceStat(stat: Stat)

    suspend fun addFocusTime(topicId: Long, focusTime: Long)

    suspend fun addBreakTime(topicId: Long, breakTime: Long)

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
    private val ioDispatcher: CoroutineDispatcher
) : StatRepository {
    override suspend fun insertStat(stat: Stat) = statDao.insertStat(stat)

    override suspend fun replaceStat(stat: Stat) = statDao.replaceStat(stat)

    override suspend fun addFocusTime(topicId: Long, focusTime: Long) =
        withContext(ioDispatcher) {
            val currentTime = LocalTime.now().toSecondOfDay()
            val secondsInDay = 24 * 60 * 60

            val quarter = when (currentTime) {
                in 0..(secondsInDay / 4) -> 1
                in (secondsInDay / 4)..(secondsInDay / 2) -> 2
                in (secondsInDay / 2)..(3 * secondsInDay / 4) -> 3
                else -> 4
            }

            statDao.addStatTimes(
                date = LocalDate.now(),
                topicId = topicId,
                focusTimeQ1 = if (quarter == 1) focusTime else 0,
                focusTimeQ2 = if (quarter == 2) focusTime else 0,
                focusTimeQ3 = if (quarter == 3) focusTime else 0,
                focusTimeQ4 = if (quarter == 4) focusTime else 0,
                breakTime = 0
            )
        }

    override suspend fun addBreakTime(topicId: Long, breakTime: Long) =
        withContext(ioDispatcher) {
            statDao.addStatTimes(
                date = LocalDate.now(),
                topicId = topicId,
                focusTimeQ1 = 0,
                focusTimeQ2 = 0,
                focusTimeQ3 = 0,
                focusTimeQ4 = 0,
                breakTime = breakTime
            )
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
}
