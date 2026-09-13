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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
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
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val timerState = MutableStateFlow(TimerState())
    val settingsState = MutableStateFlow(SettingsState())

    private val _currentTopicId = MutableStateFlow(defaultTopic.id)
    val currentTopicId: StateFlow<Long> = _currentTopicId.asStateFlow()

    private val _currentTopic = MutableStateFlow(defaultTopic)
    val currentTopic: StateFlow<Topic> = _currentTopic.asStateFlow()

    val time = MutableStateFlow(25 * 60 * 1000L)
    var timerFrequency: Float = 60f
    var colorScheme: ColorScheme = lightColorScheme()
    var timerStateSnapshot: TimerStateSnapshot =
        TimerStateSnapshot(time = 0, timerState = TimerState())

    val windowVisible = MutableStateFlow(true) // Used on desktop

    private val _topicLoaded = MutableStateFlow(false)
    val topicLoaded: StateFlow<Boolean> = _topicLoaded.asStateFlow()

    private var isFirstLoad = true

    init {
        scope.launch {
            observeCurrentTopic()
        }
        scope.launch {
            // signalled even if the load fails, so that a restore can never wait forever
            try {
                reloadSettings()
            } finally {
                _topicLoaded.value = true
            }
        }
    }

    private suspend fun observeCurrentTopic() {
        _currentTopicId
            .flatMapLatest { id -> topicRepository.observeTopicById(id).map { id to it } }
            .collect { (id, topic) ->
                if (id != _currentTopicId.value) return@collect // another topic has been selected
                if (topic != null) {
                    publishTopic(topic)
                } else { // the selected topic has been deleted
                    if (id != defaultTopic.id) {
                        _currentTopicId.value = defaultTopic.id
                        preferenceRepository.saveLongPreference(CURRENT_TOPIC_KEY, defaultTopic.id)
                    }
                    publishTopic(defaultTopic)
                }
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
            restoreCurrentTopic()
        }
    }

    suspend fun setTopic(topic: Topic) {
        if (_currentTopicId.value == topic.id) return
        _currentTopicId.value = topic.id
        publishTopic(topic)
        preferenceRepository.saveLongPreference(CURRENT_TOPIC_KEY, topic.id)
    }

    private fun publishTopic(topic: Topic) {
        val previous = _currentTopic.value
        _currentTopic.value = topic
        if (topic.id != previous.id || !topic.hasSameIntervals(previous)) refreshTimer(topic)
    }

    private fun refreshTimer(topic: Topic) {
        val currentState = timerState.value
        if (currentState.serviceRunning || currentState.infiniteFocus) return

        time.value = topic.focusTime
        timerState.update {
            it.copy(
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

    private fun Topic.hasSameIntervals(other: Topic) =
        focusTime == other.focusTime &&
                shortBreakTime == other.shortBreakTime &&
                longBreakTime == other.longBreakTime &&
                sessionLength == other.sessionLength

    private suspend fun restoreCurrentTopic() {
        val storedId = preferenceRepository.getLongPreference(CURRENT_TOPIC_KEY)
        val topic = storedId?.let { topicRepository.getTopicById(it) }
            ?: topicRepository.getTopicById(defaultTopic.id)
            ?: defaultTopic

        // the stored topic may be missing, or have never been written in the first place
        if (storedId != topic.id) {
            preferenceRepository.saveLongPreference(CURRENT_TOPIC_KEY, topic.id)
        }

        _currentTopicId.value = topic.id
        _currentTopic.value = topic
        refreshTimer(topic)
    }

    private companion object {
        const val CURRENT_TOPIC_KEY = "current_topic_id"
    }
}
