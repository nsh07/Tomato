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

    @Test
    fun `saveSetting with Boolean key updates state and preference repository`() = runBlocking {
        viewModel.saveSetting(SettingsKey.ALARM_ENABLED, false)

        awaitUntil("alarm enabled to be updated") {
            !viewModel.settingsState.value.alarmEnabled &&
                    preferenceRepository.getBooleanPreference(SettingsKey.ALARM_ENABLED.key) == false
        }
    }

    @Test
    fun `saveSetting with String key updates state and preference repository`() = runBlocking {
        viewModel.saveSetting(SettingsKey.THEME, "dark")

        awaitUntil("theme to be updated") {
            viewModel.settingsState.value.theme == "dark" &&
                    preferenceRepository.getStringPreference(SettingsKey.THEME.key) == "dark"
        }
    }

    @Test
    fun `saveSetting with Int key updates state and preference repository`() = runBlocking {
        viewModel.saveSetting(SettingsKey.FOCUS_GOAL, 120)

        awaitUntil("focus goal to be updated") {
            viewModel.settingsState.value.focusGoal == 120L &&
                    preferenceRepository.getIntPreference(SettingsKey.FOCUS_GOAL.key) == 120
        }
    }

    @Test
    fun `saveSetting with Color key updates state and preference repository`() = runBlocking {
        val testColor = Color(0xFF3B82F6)
        viewModel.saveSetting(SettingsKey.COLOR_SCHEME, testColor)

        awaitUntil("color scheme to be updated") {
            viewModel.settingsState.value.colorScheme == testColor &&
                    preferenceRepository.getColorPreference(SettingsKey.COLOR_SCHEME.key) == testColor
        }
    }

    @Test
    fun `SaveColorScheme action updates color scheme state and preference repository`() =
        runBlocking {
            val testColor = Color(0xFF3B82F6)
            viewModel.onAction(SettingsAction.SaveColorScheme(testColor))

            awaitUntil("color scheme to be updated") {
                viewModel.settingsState.value.colorScheme == testColor &&
                        preferenceRepository.getColorPreference(SettingsKey.COLOR_SCHEME.key) == testColor
            }

            viewModel.onAction(SettingsAction.SaveColorScheme(Color.White))
            awaitUntil("color scheme to be updated to White") {
                viewModel.settingsState.value.colorScheme == Color.White &&
                        preferenceRepository.getColorPreference(SettingsKey.COLOR_SCHEME.key) == Color.White
            }
        }

    @Test
    fun `all Save actions update state and preference repository`() = runBlocking {
        viewModel.onAction(SettingsAction.SaveTheme("dark"))
        awaitUntil("theme updated") {
            viewModel.settingsState.value.theme == "dark" &&
                    preferenceRepository.getStringPreference(SettingsKey.THEME.key) == "dark"
        }

        viewModel.onAction(SettingsAction.SaveAlarmSound("fake://sound"))
        awaitUntil("alarm sound updated") {
            viewModel.settingsState.value.alarmSoundUri == "fake://sound" &&
                    preferenceRepository.getStringPreference(SettingsKey.ALARM_SOUND.key) == "fake://sound"
        }

        viewModel.onAction(SettingsAction.SaveAlarmEnabled(false))
        awaitUntil("alarm enabled updated") {
            !viewModel.settingsState.value.alarmEnabled &&
                    preferenceRepository.getBooleanPreference(SettingsKey.ALARM_ENABLED.key) == false
        }

        viewModel.onAction(SettingsAction.SaveVibrateEnabled(false))
        awaitUntil("vibrate enabled updated") {
            !viewModel.settingsState.value.vibrateEnabled &&
                    preferenceRepository.getBooleanPreference(SettingsKey.VIBRATE_ENABLED.key) == false
        }

        viewModel.onAction(SettingsAction.SaveBlackTheme(true))
        awaitUntil("black theme updated") {
            viewModel.settingsState.value.blackTheme &&
                    preferenceRepository.getBooleanPreference(SettingsKey.BLACK_THEME.key) == true
        }

        viewModel.onAction(SettingsAction.SaveAodEnabled(true))
        awaitUntil("aod enabled updated") {
            viewModel.settingsState.value.aodEnabled &&
                    preferenceRepository.getBooleanPreference(SettingsKey.AOD_ENABLED.key) == true
        }

        viewModel.onAction(SettingsAction.SaveMediaVolumeForAlarm(true))
        awaitUntil("media volume for alarm updated") {
            viewModel.settingsState.value.mediaVolumeForAlarm &&
                    preferenceRepository.getBooleanPreference(SettingsKey.MEDIA_VOLUME_FOR_ALARM.key) == true
        }

        viewModel.onAction(SettingsAction.SaveSingleProgressBar(true))
        awaitUntil("single progress bar updated") {
            viewModel.settingsState.value.singleProgressBar &&
                    preferenceRepository.getBooleanPreference(SettingsKey.SINGLE_PROGRESS_BAR.key) == true
        }

        viewModel.onAction(SettingsAction.SaveSecureAod(false))
        awaitUntil("secure aod updated") {
            !viewModel.settingsState.value.secureAod &&
                    preferenceRepository.getBooleanPreference(SettingsKey.SECURE_AOD.key) == false
        }

        viewModel.onAction(SettingsAction.SaveFocusGoal(180L))
        awaitUntil("focus goal updated") {
            viewModel.settingsState.value.focusGoal == 180L &&
                    preferenceRepository.getIntPreference(SettingsKey.FOCUS_GOAL.key) == 180
        }

        viewModel.onAction(SettingsAction.SaveVibrationOnDuration(500L))
        awaitUntil("vibration on duration updated") {
            viewModel.settingsState.value.vibrationOnDuration == 500L &&
                    preferenceRepository.getIntPreference(SettingsKey.VIBRATION_ON_DURATION.key) == 500
        }

        viewModel.onAction(SettingsAction.SaveVibrationOffDuration(300L))
        awaitUntil("vibration off duration updated") {
            viewModel.settingsState.value.vibrationOffDuration == 300L &&
                    preferenceRepository.getIntPreference(SettingsKey.VIBRATION_OFF_DURATION.key) == 300
        }

        viewModel.onAction(SettingsAction.SaveVibrationAmplitude(100))
        awaitUntil("vibration amplitude updated") {
            viewModel.settingsState.value.vibrationAmplitude == 100 &&
                    preferenceRepository.getIntPreference(SettingsKey.VIBRATION_AMPLITUDE.key) == 100
        }
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
