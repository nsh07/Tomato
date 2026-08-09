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

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nsh07.pomodoro.data.Topic.Companion.defaultTopic
import org.nsh07.pomodoro.service.TimerStateSnapshot
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsState
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerMode
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerState
import org.nsh07.pomodoro.utils.getDefaultAlarmTone
import org.nsh07.pomodoro.utils.millisecondsToStr

@OptIn(ExperimentalCoroutinesApi::class)
class StateRepository(
    private val preferenceRepository: PreferenceRepository,
    private val topicRepository: TopicRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    val timerState = MutableStateFlow(TimerState())
    val settingsState = MutableStateFlow(SettingsState())

    val currentTopicId = MutableStateFlow(defaultTopic.id)

    val currentTopic: StateFlow<Topic> = currentTopicId
        .flatMapLatest { topicRepository.observeTopicById(it) }
        .map { it ?: defaultTopic } // the selected topic may have been deleted
        .stateIn(scope, SharingStarted.Eagerly, defaultTopic)

    val time = MutableStateFlow(25 * 60 * 1000L)
    var timerFrequency: Float = 60f
    var colorScheme: ColorScheme = lightColorScheme()
    var timerStateSnapshot: TimerStateSnapshot =
        TimerStateSnapshot(time = 0, timerState = TimerState())

    val windowVisible = MutableStateFlow(true) // Used on desktop

    private var isFirstLoad = true

    init {
        scope.launch {
            reloadSettings()
        }
    }

    suspend fun reloadSettings() {
        val defaults = SettingsState()

        val focusGoal = preferenceRepository.getIntPreference("focus_goal")?.toLong()
            ?: preferenceRepository.saveIntPreference("focus_goal", defaults.focusGoal.toInt())
                .toLong()

        val alarmSoundUri = (
                preferenceRepository.getStringPreference("alarm_sound")
                    ?: preferenceRepository.saveStringPreference(
                        "alarm_sound",
                        getDefaultAlarmTone().toString()
                    )
                )

        val theme = preferenceRepository.getStringPreference("theme")
            ?: preferenceRepository.saveStringPreference("theme", defaults.theme)
        val colorScheme = preferenceRepository.getColorPreference("color_scheme")
            ?: preferenceRepository.saveColorPreference("color_scheme", defaults.colorScheme)
        val blackTheme = preferenceRepository.getBooleanPreference("black_theme")
            ?: preferenceRepository.saveBooleanPreference("black_theme", defaults.blackTheme)
        val aodEnabled = preferenceRepository.getBooleanPreference("aod_enabled")
            ?: preferenceRepository.saveBooleanPreference("aod_enabled", defaults.aodEnabled)
        val alarmEnabled = preferenceRepository.getBooleanPreference("alarm_enabled")
            ?: preferenceRepository.saveBooleanPreference(
                "alarm_enabled",
                defaults.alarmEnabled
            )
        val vibrateEnabled = preferenceRepository.getBooleanPreference("vibrate_enabled")
            ?: preferenceRepository.saveBooleanPreference(
                "vibrate_enabled",
                defaults.vibrateEnabled
            )
        val mediaVolumeForAlarm =
            preferenceRepository.getBooleanPreference("media_volume_for_alarm")
                ?: preferenceRepository.saveBooleanPreference(
                    "media_volume_for_alarm",
                    defaults.mediaVolumeForAlarm
                )
        val singleProgressBar = preferenceRepository.getBooleanPreference("single_progress_bar")
            ?: preferenceRepository.saveBooleanPreference(
                "single_progress_bar",
                defaults.singleProgressBar
            )
        val secureAod = preferenceRepository.getBooleanPreference("secure_aod")
            ?: preferenceRepository.saveBooleanPreference("secure_aod", defaults.secureAod)

        val vibrationOnDuration = (preferenceRepository.getIntPreference("vibration_on_duration")
            ?: preferenceRepository.saveIntPreference(
                "vibration_on_duration",
                defaults.vibrationOnDuration.toInt()
            )).toLong()

        val vibrationOffDuration = (preferenceRepository.getIntPreference("vibration_off_duration")
            ?: preferenceRepository.saveIntPreference(
                "vibration_off_duration",
                defaults.vibrationOffDuration.toInt()
            )).toLong()

        val vibrationAmplitude = preferenceRepository.getIntPreference("vibration_amplitude")
            ?: preferenceRepository.saveIntPreference(
                "vibration_amplitude",
                defaults.vibrationAmplitude
            )

        val customWindowDecor = preferenceRepository.getBooleanPreference("custom_window_decor")
            ?: preferenceRepository.saveBooleanPreference(
                "custom_window_decor",
                defaults.customWindowDecor
            )

        settingsState.update { currentState ->
            currentState.copy(
                focusGoal = focusGoal,
                theme = theme,
                colorScheme = colorScheme,
                alarmSoundUri = alarmSoundUri,
                blackTheme = blackTheme,
                aodEnabled = aodEnabled,
                alarmEnabled = alarmEnabled,
                vibrateEnabled = vibrateEnabled,
                mediaVolumeForAlarm = mediaVolumeForAlarm,
                singleProgressBar = singleProgressBar,
                secureAod = secureAod,
                vibrationOnDuration = vibrationOnDuration,
                vibrationOffDuration = vibrationOffDuration,
                vibrationAmplitude = vibrationAmplitude,
                customWindowDecor = customWindowDecor
            )
        }

        if (isFirstLoad) {
            isFirstLoad = false
            val topic = restoreCurrentTopic()
            time.update { topic.focusTime }
            timerState.update { currentState ->
                currentState.copy(
                    timerMode = TimerMode.FOCUS,
                    timeStr = millisecondsToStr(topic.focusTime),
                    totalTime = topic.focusTime,
                    nextTimerMode = if (topic.sessionLength > 1) TimerMode.SHORT_BREAK else TimerMode.LONG_BREAK,
                    nextTimeStr = millisecondsToStr(if (topic.sessionLength > 1) topic.shortBreakTime else topic.longBreakTime),
                    currentFocusCount = 1,
                    totalFocusCount = topic.sessionLength
                )
            }
        }
    }

    suspend fun setTopic(topic: Topic) {
        if (currentTopicId.value == topic.id) return
        currentTopicId.value = topic.id
        preferenceRepository.saveLongPreference(CURRENT_TOPIC_KEY, topic.id)
        currentTopic.first { it.id == topic.id || it.id == defaultTopic.id }
    }

    private suspend fun restoreCurrentTopic(): Topic {
        val storedId = preferenceRepository.getLongPreference(CURRENT_TOPIC_KEY)
            ?: currentTopicId.value
        val topic = topicRepository.getTopicById(storedId)
            ?: topicRepository.getTopicById(defaultTopic.id)
            ?: defaultTopic

        currentTopicId.value = topic.id
        return topic
    }

    private companion object {
        const val CURRENT_TOPIC_KEY = "current_topic_id"
    }
}
