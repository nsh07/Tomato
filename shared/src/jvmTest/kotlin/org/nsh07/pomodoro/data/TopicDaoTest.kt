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

import androidx.sqlite.SQLiteException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Tests for [TopicDao]: unique topic names, and what deleting a topic does to its stats. */
class TopicDaoTest : DatabaseTest() {

    @Test
    fun `merging deletes the topic it merged away`(): Unit = runBlocking {
        val target = insertTopic("Work")
        val source = insertTopic("Reading")

        topicDao.deleteTopicMergingStats(source, target.id)

        assertNull(topicDao.getTopicById(source.id))
        assertNotNull(topicDao.getTopicById(target.id))
    }

    @Test
    fun `merging adds the times of a date both topics share`(): Unit = runBlocking {
        val target = insertTopic("Work")
        val source = insertTopic("Reading")

        statDao.insertStat(stat("2026-03-12", target.id, 1, 2, 3, 4, 5))
        statDao.insertStat(stat("2026-03-12", source.id, 10, 20, 30, 40, 50))

        topicDao.deleteTopicMergingStats(source, target.id)

        val merged = assertNotNull(statFor("2026-03-12", target.id))
        assertContentEquals(
            listOf(11L, 22L, 33L, 44L, 55L),
            listOf(
                merged.focusTimeQ1,
                merged.focusTimeQ2,
                merged.focusTimeQ3,
                merged.focusTimeQ4,
                merged.breakTime
            )
        )
    }

    @Test
    fun `merging moves a date the target does not have yet`(): Unit = runBlocking {
        val target = insertTopic("Work")
        val source = insertTopic("Reading")

        statDao.insertStat(stat("2026-03-12", source.id, 10, 20, 30, 40, 50))

        topicDao.deleteTopicMergingStats(source, target.id)

        val moved = assertNotNull(statFor("2026-03-12", target.id))
        assertContentEquals(
            listOf(10L, 20L, 30L, 40L, 50L),
            listOf(
                moved.focusTimeQ1,
                moved.focusTimeQ2,
                moved.focusTimeQ3,
                moved.focusTimeQ4,
                moved.breakTime
            )
        )
    }

    @Test
    fun `merging handles shared and unshared dates together`(): Unit = runBlocking {
        val target = insertTopic("Work")
        val source = insertTopic("Reading")

        statDao.insertStat(stat("2026-03-12", target.id, 1, 1, 1, 1, 1))
        statDao.insertStat(stat("2026-03-12", source.id, 2, 2, 2, 2, 2))
        statDao.insertStat(stat("2026-03-13", source.id, 7, 7, 7, 7, 7))
        statDao.insertStat(stat("2026-03-14", target.id, 9, 9, 9, 9, 9))

        topicDao.deleteTopicMergingStats(source, target.id)

        val stats = topicDao.getStats(target.id).sortedBy { it.date }
        assertContentEquals(
            listOf(
                LocalDate.parse("2026-03-12"),
                LocalDate.parse("2026-03-13"),
                LocalDate.parse("2026-03-14")
            ),
            stats.map { it.date }
        )
        assertContentEquals(listOf(12L, 28L, 36L), stats.map { it.totalFocusTime() })
        assertContentEquals(listOf(3L, 7L, 9L), stats.map { it.breakTime })
    }

    @Test
    fun `merging a topic with no stats leaves the target untouched`(): Unit = runBlocking {
        val target = insertTopic("Work")
        val source = insertTopic("Reading")

        statDao.insertStat(stat("2026-03-12", target.id, 1, 2, 3, 4, 5))

        topicDao.deleteTopicMergingStats(source, target.id)

        val kept = assertNotNull(statFor("2026-03-12", target.id))
        assertEquals(10L, kept.totalFocusTime())
        assertEquals(5L, kept.breakTime)
    }

    @Test
    fun `merging leaves an unrelated topic's stats alone`(): Unit = runBlocking {
        val target = insertTopic("Work")
        val source = insertTopic("Reading")
        val bystander = insertTopic("Exercise")

        statDao.insertStat(stat("2026-03-12", source.id, 1, 1, 1, 1, 1))
        statDao.insertStat(stat("2026-03-12", bystander.id, 5, 5, 5, 5, 5))

        topicDao.deleteTopicMergingStats(source, target.id)

        val untouched = assertNotNull(statFor("2026-03-12", bystander.id))
        assertEquals(20L, untouched.totalFocusTime())
        assertEquals(5L, untouched.breakTime)
    }

    @Test
    fun `merging does not lose focus time overall`(): Unit = runBlocking {
        val target = insertTopic("Work")
        val source = insertTopic("Reading")

        statDao.insertStat(stat("2026-03-12", target.id, 1, 2, 3, 4, 5))
        statDao.insertStat(stat("2026-03-12", source.id, 10, 20, 30, 40, 50))
        statDao.insertStat(stat("2026-03-13", source.id, 100, 0, 0, 0, 0))

        val before = statDao.getAllTimeTotalFocusTime().first()

        topicDao.deleteTopicMergingStats(source, target.id)

        assertEquals(before, statDao.getAllTimeTotalFocusTime().first())
    }

    @Test
    fun `deleting a topic cascades to its stats only`(): Unit = runBlocking {
        val deleted = insertTopic("Reading")
        val kept = insertTopic("Work")

        statDao.insertStat(stat("2026-03-12", deleted.id, 1, 1, 1, 1, 1))
        statDao.insertStat(stat("2026-03-12", kept.id, 2, 2, 2, 2, 2))

        topicDao.deleteTopic(deleted)

        assertTrue(topicDao.getStats(deleted.id).isEmpty())
        assertEquals(1, topicDao.getStats(kept.id).size)
    }

    @Test
    fun `inserting a name that differs only in case is ignored`(): Unit = runBlocking {
        topicDao.insertTopic(topic("Reading"))

        assertEquals(-1L, topicDao.insertTopic(topic("reading")))
        assertEquals(1, topicDao.getAllTopics().first().size)
    }

    @Test
    fun `renaming onto an existing name fails instead of merging the rows`(): Unit = runBlocking {
        topicDao.insertTopic(topic("Reading"))
        val work = insertTopic("Work")

        assertFailsWith<SQLiteException> {
            topicDao.updateTopic(work.copy(name = "reading"))
        }

        assertEquals(2, topicDao.getAllTopics().first().size)
        assertEquals("Work", assertNotNull(topicDao.getTopicById(work.id)).name)
    }

    @Test
    fun `topics are listed by name`(): Unit = runBlocking {
        topicDao.insertTopic(topic("Work"))
        topicDao.insertTopic(topic("Exercise"))
        topicDao.insertTopic(topic("Reading"))

        assertContentEquals(
            listOf("Exercise", "Reading", "Work"),
            topicDao.getAllTopics().first().map { it.name }
        )
    }
}
