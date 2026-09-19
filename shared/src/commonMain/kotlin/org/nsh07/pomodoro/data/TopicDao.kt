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

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TopicDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTopic(topic: Topic): Long

    @Update
    suspend fun updateTopic(topic: Topic)

    @Delete
    suspend fun deleteTopic(topic: Topic)

    @Transaction
    suspend fun deleteTopicMergingStats(topic: Topic, targetTopicId: Long) {
        val targetDates = getStatDates(targetTopicId).toSet()
        getStats(topic.id).forEach { stat ->
            if (stat.date in targetDates) addStatTimes(
                date = stat.date,
                topicId = targetTopicId,
                focusTimeQ1 = stat.focusTimeQ1,
                focusTimeQ2 = stat.focusTimeQ2,
                focusTimeQ3 = stat.focusTimeQ3,
                focusTimeQ4 = stat.focusTimeQ4,
                breakTime = stat.breakTime
            ) else moveStat(date = stat.date, fromTopicId = topic.id, toTopicId = targetTopicId)
        }
        deleteTopic(topic)
    }

    @Query("SELECT * FROM stat WHERE topicId = :topicId")
    suspend fun getStats(topicId: Long): List<Stat>

    @Query("SELECT date FROM stat WHERE topicId = :topicId")
    suspend fun getStatDates(topicId: Long): List<LocalDate>

    @Query(
        """
        UPDATE stat SET
            focusTimeQ1 = focusTimeQ1 + :focusTimeQ1,
            focusTimeQ2 = focusTimeQ2 + :focusTimeQ2,
            focusTimeQ3 = focusTimeQ3 + :focusTimeQ3,
            focusTimeQ4 = focusTimeQ4 + :focusTimeQ4,
            breakTime = breakTime + :breakTime
        WHERE date = :date AND topicId = :topicId
        """
    )
    suspend fun addStatTimes(
        date: LocalDate,
        topicId: Long,
        focusTimeQ1: Long,
        focusTimeQ2: Long,
        focusTimeQ3: Long,
        focusTimeQ4: Long,
        breakTime: Long
    )

    @Query("UPDATE stat SET topicId = :toTopicId WHERE date = :date AND topicId = :fromTopicId")
    suspend fun moveStat(date: LocalDate, fromTopicId: Long, toTopicId: Long)

    @Query("SELECT * FROM topic ORDER BY name ASC")
    fun getAllTopics(): Flow<List<Topic>>

    @Query("SELECT * FROM topic WHERE id = :id")
    suspend fun getTopicById(id: Long): Topic?

    @Query("SELECT * FROM topic WHERE id = :id")
    fun observeTopicById(id: Long): Flow<Topic?>

    @Query("SELECT id FROM topic")
    suspend fun getTopicIds(): List<Long>
}
