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
import kotlinx.coroutines.flow.flowOf
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

/** [TopicRepository] serving a single, fixed topic */
class FakeTopicRepository(private val topic: Topic) : TopicRepository {
    override suspend fun insertTopic(topic: Topic): Long = topic.id

    override suspend fun updateTopic(topic: Topic) {}

    override suspend fun deleteTopic(topic: Topic) {}

    override suspend fun deleteTopicMergingStats(topic: Topic, targetTopicId: Long) {}

    override fun getAllTopics(): Flow<List<Topic>> = flowOf(listOf(topic))

    override suspend fun getTopicById(id: Long): Topic? = topic.takeIf { it.id == id }

    override fun observeTopicById(id: Long): Flow<Topic?> = flowOf(topic.takeIf { it.id == id })

    override suspend fun getTopicIds(): List<Long> = listOf(topic.id)
}
