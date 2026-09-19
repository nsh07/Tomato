/*
 * Copyright (c) 2026 Nishant Mishra
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

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/** Tests for [StatDao]: the three ways a row can be written, and the aggregation queries. */
class StatDaoTest : DatabaseTest() {

    @Test
    fun `inserting a seed row never overwrites recorded time`(): Unit = runBlocking {
        val work = insertTopic("Work")
        statDao.insertStat(stat("2026-03-12", work.id, 1, 2, 3, 4, 5))

        statDao.insertStat(stat("2026-03-12", work.id))

        val kept = assertNotNull(statFor("2026-03-12", work.id))
        assertEquals(10L, kept.totalFocusTime())
        assertEquals(5L, kept.breakTime)
    }

    @Test
    fun `replacing a row overwrites it`(): Unit = runBlocking {
        val work = insertTopic("Work")
        statDao.insertStat(stat("2026-03-12", work.id, 1, 2, 3, 4, 5))

        statDao.replaceStat(stat("2026-03-12", work.id, 9, 0, 0, 0, 0))

        val replaced = assertNotNull(statFor("2026-03-12", work.id))
        assertEquals(9L, replaced.totalFocusTime())
        assertEquals(0L, replaced.breakTime)
    }

    @Test
    fun `adding times creates the row when it is missing`(): Unit = runBlocking {
        val work = insertTopic("Work")

        statDao.addStatTimes(LocalDate.parse("2026-03-12"), work.id, 1, 2, 3, 4, 5)

        val created = assertNotNull(statFor("2026-03-12", work.id))
        assertEquals(10L, created.totalFocusTime())
        assertEquals(5L, created.breakTime)
    }

    @Test
    fun `adding times accumulates onto an existing row`(): Unit = runBlocking {
        val work = insertTopic("Work")
        statDao.insertStat(stat("2026-03-12", work.id, 1, 2, 3, 4, 5))

        statDao.addStatTimes(LocalDate.parse("2026-03-12"), work.id, 10, 20, 30, 40, 50)

        val summed = assertNotNull(statFor("2026-03-12", work.id))
        assertContentEquals(
            listOf(11L, 22L, 33L, 44L, 55L),
            listOf(
                summed.focusTimeQ1,
                summed.focusTimeQ2,
                summed.focusTimeQ3,
                summed.focusTimeQ4,
                summed.breakTime
            )
        )
    }

    @Test
    fun `adding times keeps each topic's row separate`(): Unit = runBlocking {
        val work = insertTopic("Work")
        val reading = insertTopic("Reading")
        val date = LocalDate.parse("2026-03-12")

        statDao.addStatTimes(date, work.id, 1, 0, 0, 0, 0)
        statDao.addStatTimes(date, reading.id, 5, 0, 0, 0, 0)

        assertEquals(1L, assertNotNull(statFor("2026-03-12", work.id)).totalFocusTime())
        assertEquals(5L, assertNotNull(statFor("2026-03-12", reading.id)).totalFocusTime())
    }

    @Test
    fun `adding times notifies observers of the stat table`(): Unit = runBlocking {
        val work = insertTopic("Work")
        val date = LocalDate.parse("2026-03-12")
        val stats = statDao.getStat(date)

        // The upsert is a raw @Query, so Room has to infer from the SQL that it writes `stat`
        statDao.addStatTimes(date, work.id, 5, 0, 0, 0, 0)
        assertEquals(5L, assertNotNull(stats.first()).totalFocusTime())

        statDao.addStatTimes(date, work.id, 7, 0, 0, 0, 0)
        assertEquals(12L, assertNotNull(stats.first()).totalFocusTime())
    }

    @Test
    fun `the stat for a date sums every topic`(): Unit = runBlocking {
        val work = insertTopic("Work")
        val reading = insertTopic("Reading")

        statDao.insertStat(stat("2026-03-12", work.id, 1, 2, 3, 4, 5))
        statDao.insertStat(stat("2026-03-12", reading.id, 10, 20, 30, 40, 50))

        val total = assertNotNull(statDao.getStat(LocalDate.parse("2026-03-12")).first())

        assertEquals(Stat.MERGED_TOPIC_ID, total.topicId)
        assertEquals(110L, total.totalFocusTime())
        assertEquals(55L, total.breakTime)
    }

    @Test
    fun `the last N days give one summed row per date`(): Unit = runBlocking {
        val work = insertTopic("Work")
        val reading = insertTopic("Reading")

        statDao.insertStat(stat("2026-03-12", work.id, 1, 0, 0, 0, 1))
        statDao.insertStat(stat("2026-03-12", reading.id, 2, 0, 0, 0, 2))
        statDao.insertStat(stat("2026-03-13", work.id, 4, 0, 0, 0, 4))

        val stats = statDao.getLastNDaysStats(7).first()

        assertContentEquals(
            listOf(LocalDate.parse("2026-03-13"), LocalDate.parse("2026-03-12")),
            stats.map { it.date }
        )
        assertContentEquals(listOf(4L, 3L), stats.map { it.totalFocusTime() })
    }

    @Test
    fun `the last N days limit counts dates, not topic rows`(): Unit = runBlocking {
        val work = insertTopic("Work")
        val reading = insertTopic("Reading")

        listOf("2026-03-12", "2026-03-13", "2026-03-14").forEach { date ->
            statDao.insertStat(stat(date, work.id, 1, 0, 0, 0, 0))
            statDao.insertStat(stat(date, reading.id, 1, 0, 0, 0, 0))
        }

        assertEquals(2, statDao.getLastNDaysStats(2).first().size)
    }

    @Test
    fun `the all time total spans every topic`(): Unit = runBlocking {
        val work = insertTopic("Work")
        val reading = insertTopic("Reading")

        statDao.insertStat(stat("2026-03-12", work.id, 1, 2, 3, 4, 99))
        statDao.insertStat(stat("2026-03-13", reading.id, 5, 0, 0, 0, 99))

        assertEquals(15L, statDao.getAllTimeTotalFocusTime().first())
    }

    @Test
    fun `the daily average folds each date's topics together first`(): Unit = runBlocking {
        val work = insertTopic("Work")
        val reading = insertTopic("Reading")

        // Dates totalling 10 and 20, so the average is 15, not the 7 that averaging rows gives
        statDao.insertStat(stat("2026-03-12", work.id, 4, 0, 0, 0, 0))
        statDao.insertStat(stat("2026-03-12", reading.id, 6, 0, 0, 0, 0))
        statDao.insertStat(stat("2026-03-13", work.id, 8, 0, 0, 0, 0))
        statDao.insertStat(stat("2026-03-13", reading.id, 12, 0, 0, 0, 0))

        val average = assertNotNull(statDao.getLastNDaysAvgStats(7).first())

        assertEquals(15L, average.focusTimeQ1)
    }
}
