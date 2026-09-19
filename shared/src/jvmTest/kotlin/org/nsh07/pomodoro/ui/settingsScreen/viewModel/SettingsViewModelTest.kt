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

package org.nsh07.pomodoro.ui.settingsScreen.viewModel

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import org.nsh07.pomodoro.billing.BillingManager
import org.nsh07.pomodoro.data.StateRepository
import org.nsh07.pomodoro.data.Topic
import org.nsh07.pomodoro.data.Topic.Companion.DEFAULT_TOPIC_ID
import org.nsh07.pomodoro.data.TopicShape
import org.nsh07.pomodoro.service.FakePreferenceRepository
import org.nsh07.pomodoro.service.FakeStatRepository
import org.nsh07.pomodoro.service.FakeTopicRepository
import org.nsh07.pomodoro.service.TimerHelper
import org.nsh07.pomodoro.ui.settingsScreen.components.MinuteInputs
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerAction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

class SettingsViewModelTest {

    private val storedDefaultTopic = topic(id = DEFAULT_TOPIC_ID, name = "Default", minutes = 30)
    private val work = topic(id = 42L, name = "Work", minutes = 50)
    private val study = topic(id = 43L, name = "Study", minutes = 15)

    private val topicRepository = FakeTopicRepository(storedDefaultTopic, work, study)
    private val preferenceRepository = FakePreferenceRepository(currentTopicId = work.id)

    // Created together, before the saved topic has been restored, as on startup
    private val stateRepository = StateRepository(preferenceRepository, topicRepository)
    private val viewModel = SettingsViewModel(
        billingManager = object : BillingManager {
            override val isPlus: StateFlow<Boolean> = MutableStateFlow(false)
        },
        preferenceRepository = preferenceRepository,
        stateRepository = stateRepository,
        statRepository = FakeStatRepository(),
        topicRepository = topicRepository,
        timerHelper = object : TimerHelper {
            override fun onAction(action: TimerAction) {}
        }
    )

    @Test
    fun `the restored topic is edited, without being written back`() = runBlocking {
        awaitUntil("the restored topic to be edited") { viewModel.editingTopic.value == work }

        assertEquals(MinuteInputs("50", "5", "15"), viewModel.minuteInputs)
        assertEquals(4f, viewModel.sessionsSliderState.value)
        assertEquals(0, topicRepository.updates)
        assertEquals(storedDefaultTopic, topicRepository.getTopicById(DEFAULT_TOPIC_ID))
    }

    @Test
    fun `a valid duration is saved as typed`() = runBlocking {
        awaitUntil("the restored topic to be edited") { viewModel.editingTopic.value == work }

        viewModel.onAction(
            SettingsAction.SetEditingTopicMinutes(viewModel.minuteInputs.copy(focus = "40"))
        )

        assertEquals("40", viewModel.minuteInputs.focus)
        awaitUntil("the focus time to be saved") {
            topicRepository.getTopicById(work.id)?.focusTime == 40 * MINUTE
        }
    }

    @Test
    fun `an invalid duration stays in its field but is not saved`() = runBlocking {
        awaitUntil("the restored topic to be edited") { viewModel.editingTopic.value == work }

        viewModel.onAction(
            SettingsAction.SetEditingTopicMinutes(viewModel.minuteInputs.copy(focus = ""))
        )
        assertEquals("", viewModel.minuteInputs.focus)

        // A later, valid edit is saved on its own, leaving the invalid duration alone
        viewModel.onAction(
            SettingsAction.SetEditingTopicMinutes(viewModel.minuteInputs.copy(shortBreak = "7"))
        )
        awaitUntil("the short break time to be saved") {
            topicRepository.getTopicById(work.id)?.shortBreakTime == 7 * MINUTE
        }
        assertEquals(work.focusTime, topicRepository.getTopicById(work.id)?.focusTime)
        assertEquals("", viewModel.minuteInputs.focus)
    }

    @Test
    fun `edits are debounced into a single write`() = runBlocking {
        awaitUntil("the restored topic to be edited") { viewModel.editingTopic.value == work }

        for (typed in listOf("4", "45", "450")) {
            viewModel.onAction(
                SettingsAction.SetEditingTopicMinutes(viewModel.minuteInputs.copy(focus = typed))
            )
        }

        awaitUntil("the focus time to be saved") {
            topicRepository.getTopicById(work.id)?.focusTime == 450 * MINUTE
        }
        assertEquals(1, topicRepository.updates)
    }

    @Test
    fun `an edit still being debounced is saved when the settings are closed`() = runBlocking {
        awaitUntil("the restored topic to be edited") { viewModel.editingTopic.value == work }

        viewModel.onAction(
            SettingsAction.SetEditingTopicMinutes(viewModel.minuteInputs.copy(focus = "40"))
        )
        viewModel.onSettingsClosed()

        awaitUntil("the focus time to be saved") {
            topicRepository.getTopicById(work.id)?.focusTime == 40 * MINUTE
        }
        assertEquals(1, topicRepository.updates)
    }

    @Test
    fun `the topic being edited follows the current topic until one is picked`() = runBlocking {
        awaitUntil("the restored topic to be edited") { viewModel.editingTopic.value == work }

        stateRepository.setTopic(study)
        awaitUntil("the new current topic to be edited") {
            viewModel.editingTopic.value == study
        }
        assertEquals(MinuteInputs(study), viewModel.minuteInputs)

        viewModel.onAction(SettingsAction.SetEditingTopic(work))
        stateRepository.setTopic(storedDefaultTopic)
        awaitUntil("the current topic to change") {
            stateRepository.currentTopic.value == storedDefaultTopic
        }
        assertEquals(work, viewModel.editingTopic.value)
        assertEquals(MinuteInputs(work), viewModel.minuteInputs)
    }

    private suspend fun awaitUntil(description: String, condition: suspend () -> Boolean) {
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
    }
}
