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
import org.nsh07.pomodoro.data.Topic.Companion.DEFAULT_TOPIC_ID
import org.nsh07.pomodoro.data.Topic.Companion.defaultTopic
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Tests for [SeedDefaultTopicCallback]. A fresh install has to open with a topic at
 * [DEFAULT_TOPIC_ID]: the timer falls back to it, and deleted topics are merged into it.
 */
class SeedDefaultTopicCallbackTest : DatabaseTest(seedsDefaultTopic = true) {

    @Test
    fun `a new database holds exactly the default topic`(): Unit = runBlocking {
        assertEquals(listOf(defaultTopic), topicDao.getAllTopics().first())
    }

    @Test
    fun `the seeded topic is the one the app falls back to`(): Unit = runBlocking {
        val seeded = assertNotNull(topicDao.getTopicById(DEFAULT_TOPIC_ID))

        assertEquals(defaultTopic, seeded)
    }

    @Test
    fun `topics added afterwards do not take the default topic's id`(): Unit = runBlocking {
        assertEquals(DEFAULT_TOPIC_ID + 1, topicDao.insertTopic(topic("Work")))
    }

    @Test
    fun `the seeded topic can record stats`(): Unit = runBlocking {
        statDao.insertStat(stat("2026-03-12", DEFAULT_TOPIC_ID, 1, 2, 3, 4, 5))

        assertEquals(10L, assertNotNull(statFor("2026-03-12", DEFAULT_TOPIC_ID)).totalFocusTime())
    }
}
