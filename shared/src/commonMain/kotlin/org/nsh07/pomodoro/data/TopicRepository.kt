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

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

interface TopicRepository {
    suspend fun insertTopic(topic: Topic): Long
    suspend fun updateTopic(topic: Topic)
    suspend fun deleteTopic(topic: Topic)
    fun getAllTopics(): Flow<List<Topic>>
    suspend fun getTopicById(id: String): Topic?
    fun observeTopicById(id: String): Flow<Topic?>
    suspend fun getTopicIds(): List<String>
}

class AppTopicRepository(
    private val topicDao: TopicDao,
    private val ioDispatcher: CoroutineDispatcher
) : TopicRepository {
    override suspend fun insertTopic(topic: Topic): Long = withContext(ioDispatcher) {
        topicDao.insertTopic(topic)
    }

    override suspend fun updateTopic(topic: Topic) = withContext(ioDispatcher) {
        topicDao.updateTopic(topic)
    }

    override suspend fun deleteTopic(topic: Topic) = withContext(ioDispatcher) {
        topicDao.deleteTopic(topic)
    }

    override fun getAllTopics(): Flow<List<Topic>> = topicDao.getAllTopics()

    override suspend fun getTopicById(id: String): Topic? = withContext(ioDispatcher) {
        topicDao.getTopicById(id)
    }

    override fun observeTopicById(id: String): Flow<Topic?> = topicDao.observeTopicById(id)

    override suspend fun getTopicIds(): List<String> = withContext(ioDispatcher) {
        topicDao.getTopicIds()
    }
}
