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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Tests for [AppTopicRepository], which reports a rejected write instead of throwing. */
class TopicRepositoryTest : DatabaseTest() {

    private val repository by lazy { AppTopicRepository(topicDao, Dispatchers.IO) }

    @Test
    fun `updating onto a taken name is reported instead of thrown`(): Unit = runBlocking {
        topicDao.insertTopic(topic("Reading"))
        val work = insertTopic("Work")

        assertFalse(repository.updateTopic(work.copy(name = "reading")))

        assertEquals("Work", assertNotNull(topicDao.getTopicById(work.id)).name)
        assertEquals(2, topicDao.getAllTopics().first().size)
    }

    @Test
    fun `a rejected update leaves the rest of the topic alone`(): Unit = runBlocking {
        topicDao.insertTopic(topic("Reading"))
        val work = insertTopic("Work")

        repository.updateTopic(work.copy(name = "reading", shape = TopicShape.HEART))

        assertEquals(TopicShape.CIRCLE, assertNotNull(topicDao.getTopicById(work.id)).shape)
    }

    @Test
    fun `a valid update is applied`(): Unit = runBlocking {
        val work = insertTopic("Work")

        assertTrue(repository.updateTopic(work.copy(name = "Deep work", focusTime = 1234)))

        val updated = assertNotNull(topicDao.getTopicById(work.id))
        assertEquals("Deep work", updated.name)
        assertEquals(1234L, updated.focusTime)
    }

    @Test
    fun `renaming a topic to a different case of its own name is allowed`(): Unit = runBlocking {
        val work = insertTopic("Work")

        assertTrue(repository.updateTopic(work.copy(name = "WORK")))

        assertEquals("WORK", assertNotNull(topicDao.getTopicById(work.id)).name)
    }
}
