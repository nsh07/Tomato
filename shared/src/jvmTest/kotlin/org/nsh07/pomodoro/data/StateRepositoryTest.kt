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

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import org.nsh07.pomodoro.data.Topic.Companion.DEFAULT_TOPIC_ID
import org.nsh07.pomodoro.data.Topic.Companion.defaultTopic
import org.nsh07.pomodoro.service.FakePreferenceRepository
import org.nsh07.pomodoro.service.FakeTopicRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.time.Duration.Companion.milliseconds

class StateRepositoryTest {

    /** The seeded default topic, edited by the user so that it differs from [defaultTopic] */
    private val storedDefaultTopic = topic(id = DEFAULT_TOPIC_ID, name = "Default", minutes = 30)

    private val work = topic(id = 42L, name = "Work", minutes = 50)
    private val study = topic(id = 43L, name = "Study", minutes = 15)

    @Test
    fun `the restored topic is published before the first load completes`() = runBlocking {
        val stateRepository = stateRepository(preferenceRepository(work.id))

        // topicLoaded is the last thing the initial load sets, so by then currentTopic must hold
        // the restored topic already
        awaitLoad(stateRepository)
        assertEquals(work, stateRepository.currentTopic.value)
        assertEquals(work.id, stateRepository.currentTopicId.value)
    }

    @Test
    fun `switching topic publishes it before setTopic returns`() = runBlocking {
        // Without database updates, currentTopic only holds what StateRepository publishes itself
        val stateRepository = stateRepository(topicRepository = NoUpdatesTopicRepository())
        awaitLoad(stateRepository)

        // Switching away from the default topic used to wait for a database emission that the
        // default topic already in currentTopic satisfied, so the switch had not taken effect yet
        stateRepository.setTopic(work)

        assertEquals(work, stateRepository.currentTopic.value)
        assertEquals(work.id, stateRepository.currentTopicId.value)
    }

    @Test
    fun `switching topic is persisted`() = runBlocking {
        val preferenceRepository = preferenceRepository(DEFAULT_TOPIC_ID)
        val stateRepository = stateRepository(preferenceRepository)
        awaitLoad(stateRepository)

        stateRepository.setTopic(study)

        assertEquals(study.id, preferenceRepository.getLongPreference(CURRENT_TOPIC_KEY))
    }

    @Test
    fun `edits to the selected topic are picked up`() = runBlocking {
        val topicRepository = topicRepository()
        val stateRepository = stateRepository(preferenceRepository(work.id), topicRepository)
        awaitLoad(stateRepository)

        val edited = work.copy(focusTime = 5 * MINUTE)
        topicRepository.updateTopic(edited)

        awaitUntil("the edited topic to be published") {
            stateRepository.currentTopic.value == edited
        }
    }

    @Test
    fun `editing the selected topic sets the timer up again`() = runBlocking {
        val topicRepository = topicRepository()
        val stateRepository = stateRepository(preferenceRepository(work.id), topicRepository)
        awaitLoad(stateRepository)

        val edited = work.copy(focusTime = 5 * MINUTE, sessionLength = 2)
        topicRepository.updateTopic(edited)

        awaitUntil("the timer to be set up for the edited topic") {
            stateRepository.timerState.value.totalTime == edited.focusTime
        }
        assertEquals(edited.focusTime, stateRepository.time.value)
        assertEquals("05:00", stateRepository.timerState.value.timeStr)
        assertEquals(edited.sessionLength, stateRepository.timerState.value.totalFocusCount)
    }

    @Test
    fun `switching topic sets the timer up for it`() = runBlocking {
        val stateRepository = stateRepository(topicRepository = NoUpdatesTopicRepository())
        awaitLoad(stateRepository)

        stateRepository.setTopic(study)

        assertEquals(study.focusTime, stateRepository.timerState.value.totalTime)
        assertEquals(study.focusTime, stateRepository.time.value)
    }

    @Test
    fun `an edit that leaves the intervals alone does not set the timer up again`() = runBlocking {
        val topicRepository = topicRepository()
        val stateRepository = stateRepository(preferenceRepository(work.id), topicRepository)
        awaitLoad(stateRepository)
        stateRepository.timerState.update { it.copy(currentFocusCount = 3) }

        val renamed = work.copy(name = "Work again")
        topicRepository.updateTopic(renamed)

        awaitUntil("the renamed topic to be published") {
            stateRepository.currentTopic.value == renamed
        }
        assertEquals(3, stateRepository.timerState.value.currentFocusCount)
    }

    @Test
    fun `a running timer is left alone`() = runBlocking {
        val topicRepository = topicRepository()
        val stateRepository = stateRepository(preferenceRepository(work.id), topicRepository)
        awaitLoad(stateRepository)
        stateRepository.timerState.update { it.copy(timerRunning = true) }

        val edited = work.copy(focusTime = 5 * MINUTE)
        topicRepository.updateTopic(edited)

        awaitUntil("the edited topic to be published") {
            stateRepository.currentTopic.value == edited
        }
        assertNotEquals(edited.focusTime, stateRepository.timerState.value.totalTime)
        assertEquals(work.focusTime, stateRepository.timerState.value.totalTime)
    }

    @Test
    fun `deleting the selected topic selects the default topic`() = runBlocking {
        val preferenceRepository = preferenceRepository(work.id)
        val topicRepository = topicRepository()
        val stateRepository = stateRepository(preferenceRepository, topicRepository)
        awaitLoad(stateRepository)

        topicRepository.deleteTopic(work)

        // The selection falls back to the stored default topic, not to the hardcoded one, and the
        // id, the topic and the persisted preference all agree on it afterwards
        awaitUntil("the default topic to be selected") {
            stateRepository.currentTopic.value == storedDefaultTopic
        }
        assertEquals(DEFAULT_TOPIC_ID, stateRepository.currentTopicId.value)
        assertEquals(DEFAULT_TOPIC_ID, preferenceRepository.getLongPreference(CURRENT_TOPIC_KEY))
    }

    @Test
    fun `a stored topic that no longer exists falls back to the default topic`() = runBlocking {
        val preferenceRepository = preferenceRepository(404L)
        val stateRepository = stateRepository(preferenceRepository)

        awaitLoad(stateRepository)
        assertEquals(storedDefaultTopic, stateRepository.currentTopic.value)
        assertEquals(DEFAULT_TOPIC_ID, stateRepository.currentTopicId.value)
        assertEquals(DEFAULT_TOPIC_ID, preferenceRepository.getLongPreference(CURRENT_TOPIC_KEY))
    }

    private fun topicRepository() = FakeTopicRepository(storedDefaultTopic, work, study)

    private fun stateRepository(
        preferenceRepository: FakePreferenceRepository = preferenceRepository(DEFAULT_TOPIC_ID),
        topicRepository: TopicRepository = topicRepository()
    ) = StateRepository(preferenceRepository, topicRepository)

    /** [TopicRepository] that never publishes an update for the topic being observed */
    private inner class NoUpdatesTopicRepository(
        private val delegate: TopicRepository = topicRepository()
    ) : TopicRepository by delegate {
        override fun observeTopicById(id: Long): Flow<Topic?> = emptyFlow()
    }

    private fun preferenceRepository(storedId: Long) =
        FakePreferenceRepository(currentTopicId = storedId)

    /** Waits for the load started by the [StateRepository] constructor to finish */
    private suspend fun awaitLoad(stateRepository: StateRepository) =
        withTimeout(TIMEOUT.milliseconds) { stateRepository.topicLoaded.first { it } }

    private suspend fun awaitUntil(description: String, condition: () -> Boolean) {
        try {
            withTimeout(TIMEOUT.milliseconds) {
                while (!condition()) yield()
            }
        } catch (e: Exception) {
            throw AssertionError("timed out waiting for $description", e)
        }
    }

    private fun topic(id: Long, name: String, minutes: Long) = Topic(
        id = id,
        name = name,
        color = Color.White,
        shape = TopicShape.CIRCLE,
        focusTime = minutes * MINUTE,
        shortBreakTime = 5 * MINUTE,
        longBreakTime = 15 * MINUTE,
        sessionLength = 4,
        autostartNextSession = false,
        dndEnabled = false
    )

    private companion object {
        const val MINUTE = 60_000L
        const val TIMEOUT = 5_000L
        const val CURRENT_TOPIC_KEY = "current_topic_id"
    }
}
