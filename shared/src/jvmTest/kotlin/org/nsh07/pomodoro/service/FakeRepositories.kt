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

package org.nsh07.pomodoro.service

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.nsh07.pomodoro.data.PreferenceRepository
import org.nsh07.pomodoro.data.Stat
import org.nsh07.pomodoro.data.StatRepository
import org.nsh07.pomodoro.data.StatTime
import org.nsh07.pomodoro.data.Topic
import org.nsh07.pomodoro.data.TopicRepository
import java.time.LocalDate

/** In-memory [StatRepository] that records the totals written to it */
class FakeStatRepository : StatRepository {
    var focusTime = 0L
        private set
    var breakTime = 0L
        private set

    override suspend fun insertStat(stat: Stat) {}

    override suspend fun replaceStat(stat: Stat) {}

    override suspend fun addFocusTime(topicId: Long, focusTime: Long) {
        this.focusTime += focusTime
    }

    override suspend fun addBreakTime(topicId: Long, breakTime: Long) {
        this.breakTime += breakTime
    }

    override fun getTodayStat(): Flow<Stat?> = flowOf(null)

    override fun getLastNDaysStats(n: Int): Flow<List<Stat>> = flowOf(emptyList())

    override fun getLastNDaysAverageFocusTimes(n: Int): Flow<StatTime?> = flowOf(null)

    override fun getAllTimeTotalFocusTime(): Flow<Long?> = flowOf(focusTime)

    override suspend fun getLastDate(): LocalDate? = null

    override suspend fun deleteAllStats() {}
}

/** [PreferenceRepository] backed by a map, pre-filled to avoid platform-specific defaults */
class FakePreferenceRepository(currentTopicId: Long) : PreferenceRepository {
    private val values = mutableMapOf<String, Any>(
        "alarm_sound" to "fake://alarm",
        "current_topic_id" to currentTopicId
    )

    override suspend fun saveIntPreference(key: String, value: Int): Int =
        value.also { values[key] = it }

    override suspend fun saveLongPreference(key: String, value: Long): Long =
        value.also { values[key] = it }

    override suspend fun saveBooleanPreference(key: String, value: Boolean): Boolean =
        value.also { values[key] = it }

    override suspend fun saveStringPreference(key: String, value: String): String =
        value.also { values[key] = it }

    override suspend fun saveColorPreference(key: String, value: Color): Color =
        value.also { values[key] = it }

    override suspend fun getIntPreference(key: String): Int? = values[key] as? Int

    override suspend fun getLongPreference(key: String): Long? = values[key] as? Long

    override suspend fun getBooleanPreference(key: String): Boolean? = values[key] as? Boolean

    override fun getBooleanPreferenceFlow(key: String): Flow<Boolean> =
        flowOf(values[key] as? Boolean ?: false)

    override suspend fun getStringPreference(key: String): String? = values[key] as? String

    override suspend fun getColorPreference(key: String): Color? = values[key] as? Color

    override fun getStringPreferenceFlow(key: String): Flow<String> =
        flowOf(values[key] as? String ?: "")

    override suspend fun resetSettings() {
        values.clear()
    }
}

/** [TimerStateStore] kept in memory, so that a session can outlive its [TimerManager] */
class FakeTimerStateStore : TimerStateStore {
    private var stored: PersistedTimerState? = null

    override fun load(): PersistedTimerState? = stored

    override fun save(state: PersistedTimerState) {
        stored = state
    }
}

/** In-memory [TopicRepository], whose contents may change while a test runs */
class FakeTopicRepository(vararg topics: Topic) : TopicRepository {
    private val topics = MutableStateFlow(topics.associateBy { it.id })

    /** The number of topics written with [updateTopic] */
    var updates = 0
        private set

    override suspend fun insertTopic(topic: Topic): Long =
        topic.id.also { id -> this.topics.update { it + (id to topic) } }

    override suspend fun updateTopic(topic: Topic): Boolean {
        updates++
        topics.update { it + (topic.id to topic) }
        return true
    }

    override suspend fun deleteTopic(topic: Topic) {
        topics.update { it - topic.id }
    }

    override suspend fun deleteTopicMergingStats(topic: Topic, targetTopicId: Long) =
        deleteTopic(topic)

    override fun getAllTopics(): Flow<List<Topic>> = topics.map { it.values.toList() }

    override suspend fun getTopicById(id: Long): Topic? = topics.value[id]

    override fun observeTopicById(id: Long): Flow<Topic?> =
        topics.map { it[id] }.distinctUntilChanged()

    override suspend fun getTopicIds(): List<Long> = topics.value.keys.toList()
}
