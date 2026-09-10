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
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

/**
 * A real [AppDatabase] on a temporary file, rebuilt for every test. It starts empty unless
 * [seedsDefaultTopic] is set, so that tests can assert on topic counts and row ids freely.
 */
abstract class DatabaseTest(private val seedsDefaultTopic: Boolean = false) {

    private lateinit var databaseDirectory: Path

    protected lateinit var database: AppDatabase
        private set

    protected val topicDao: TopicDao get() = database.topicDao()
    protected val statDao: StatDao get() = database.statDao()

    @BeforeTest
    fun createTestDatabase() {
        databaseDirectory = Files.createTempDirectory("tomato-database-test")
        database = Room
            .databaseBuilder<AppDatabase>(
                name = databaseDirectory.resolve("test.db").toString()
            )
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .apply { if (seedsDefaultTopic) addCallback(SeedDefaultTopicCallback) }
            .build()
    }

    @OptIn(ExperimentalPathApi::class)
    @AfterTest
    fun deleteTestDatabase() {
        database.close()
        databaseDirectory.deleteRecursively()
    }

    protected fun topic(name: String, id: Long = 0) = Topic(
        id = id,
        name = name,
        color = Color.White,
        shape = TopicShape.CIRCLE,
        focusTime = 25 * 60 * 1000L,
        shortBreakTime = 5 * 60 * 1000L,
        longBreakTime = 15 * 60 * 1000L,
        sessionLength = 4,
        autostartNextSession = false,
        dndEnabled = false
    )

    protected suspend fun insertTopic(name: String): Topic =
        topic(name).let { it.copy(id = topicDao.insertTopic(it)) }

    protected fun stat(
        date: String,
        topicId: Long,
        q1: Long = 0,
        q2: Long = 0,
        q3: Long = 0,
        q4: Long = 0,
        breakTime: Long = 0
    ) = Stat(LocalDate.parse(date), topicId, q1, q2, q3, q4, breakTime)

    protected suspend fun statFor(date: String, topicId: Long): Stat? =
        topicDao.getStats(topicId).find { it.date == LocalDate.parse(date) }
}
