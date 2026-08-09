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

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.SQLITE_DATA_NULL
import androidx.sqlite.SQLITE_DATA_TEXT
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import org.nsh07.pomodoro.data.Topic.Companion.DEFAULT_TOPIC_ID
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MigrationTest {

    private val databaseDirectory: Path = Files.createTempDirectory("tomato-migration")

    private val helper = MigrationTestHelper(
        schemaDirectoryPath = Path.of(System.getProperty("room.schemaDir")),
        databasePath = databaseDirectory.resolve("migration-test.db"),
        driver = BundledSQLiteDriver(),
        databaseClass = AppDatabase::class
    )

    @OptIn(ExperimentalPathApi::class)
    @AfterTest
    fun deleteTestDatabase() {
        databaseDirectory.deleteRecursively()
    }

    @Test
    fun `migrating 2 to 3 matches the exported schema`() {
        helper.createDatabase(2).close()

        helper.runMigrationsAndValidate(3, listOf(MIGRATION_2_3)).close()
    }

    @Test
    fun `migrating 1 to 3 matches the exported schema`() {
        helper.createDatabase(1).close()

        helper.runMigrationsAndValidate(3, listOf(MIGRATION_2_3)).close()
    }

    @Test
    fun `migrating 2 to 3 seeds the default topic from the global preferences`() {
        helper.createDatabase(2).use { db ->
            db.execSQL("INSERT INTO int_preference (key, value) VALUES ('focus_time', 1800000)")
            db.execSQL("INSERT INTO int_preference (key, value) VALUES ('short_break_time', 600000)")
            db.execSQL("INSERT INTO int_preference (key, value) VALUES ('long_break_time', 1200000)")
            db.execSQL("INSERT INTO int_preference (key, value) VALUES ('session_length', 6)")
            db.execSQL("INSERT INTO boolean_preference (key, value) VALUES ('autostart_next_session', 1)")
            db.execSQL("INSERT INTO boolean_preference (key, value) VALUES ('dnd_enabled', 1)")
        }

        helper.runMigrationsAndValidate(3, listOf(MIGRATION_2_3)).use { db ->
            val topic = db.selectRow(
                """
                SELECT id, name, shape, focusTime, shortBreakTime, longBreakTime, sessionLength,
                       autostartNextSession, dndEnabled
                FROM topic
                """
            )

            assertContentEquals(
                listOf(
                    DEFAULT_TOPIC_ID,
                    Topic.defaultTopic.name,
                    Topic.defaultTopic.shape.name,
                    1800000L,
                    600000L,
                    1200000L,
                    6L,
                    1L,
                    1L
                ),
                topic
            )
        }
    }

    @Test
    fun `migrating 2 to 3 falls back to the built-in defaults when no preferences exist`() {
        helper.createDatabase(2).close()

        helper.runMigrationsAndValidate(3, listOf(MIGRATION_2_3)).use { db ->
            val topic = db.selectRow(
                """
                SELECT focusTime, shortBreakTime, longBreakTime, sessionLength,
                       autostartNextSession, dndEnabled
                FROM topic
                """
            )

            assertContentEquals(listOf(1500000L, 300000L, 900000L, 4L, 0L, 0L), topic)
        }
    }

    @Test
    fun `migrating 2 to 3 moves existing stats onto the default topic`() {
        helper.createDatabase(2).use { db ->
            db.execSQL(
                """
                INSERT INTO stat (date, focusTimeQ1, focusTimeQ2, focusTimeQ3, focusTimeQ4, breakTime)
                VALUES ('2026-03-12', 1617943, 5704591, 556490, 1200498, 3939448),
                       ('2026-03-13', 1128282, 4590524, 100, 200, 300)
                """
            )
        }

        helper.runMigrationsAndValidate(3, listOf(MIGRATION_2_3)).use { db ->
            assertEquals(2L, db.selectLong("SELECT COUNT(*) FROM stat"))
            assertEquals(
                2L,
                db.selectLong("SELECT COUNT(*) FROM stat WHERE topicId = $DEFAULT_TOPIC_ID")
            )

            val stat = db.selectRow(
                """
                SELECT focusTimeQ1, focusTimeQ2, focusTimeQ3, focusTimeQ4, breakTime
                FROM stat WHERE date = '2026-03-12'
                """
            )

            assertContentEquals(listOf(1617943L, 5704591L, 556490L, 1200498L, 3939448L), stat)
        }
    }

    @Test
    fun `migrating 2 to 3 clears the preferences that moved onto the topic`() {
        helper.createDatabase(2).use { db ->
            db.execSQL("INSERT INTO int_preference (key, value) VALUES ('focus_time', 1800000)")
            db.execSQL("INSERT INTO int_preference (key, value) VALUES ('session_length', 6)")
            db.execSQL("INSERT INTO int_preference (key, value) VALUES ('theme', 2)")
            db.execSQL("INSERT INTO boolean_preference (key, value) VALUES ('dnd_enabled', 1)")
            db.execSQL("INSERT INTO boolean_preference (key, value) VALUES ('black_theme', 1)")
        }

        helper.runMigrationsAndValidate(3, listOf(MIGRATION_2_3)).use { db ->
            assertEquals(
                0L,
                db.selectLong(
                    """
                    SELECT COUNT(*) FROM int_preference
                    WHERE key IN ('focus_time', 'short_break_time', 'long_break_time', 'session_length')
                    """
                )
            )
            assertEquals(
                0L,
                db.selectLong(
                    """
                    SELECT COUNT(*) FROM boolean_preference
                    WHERE key IN ('autostart_next_session', 'dnd_enabled')
                    """
                )
            )

            assertEquals(2L, db.selectLong("SELECT value FROM int_preference WHERE key = 'theme'"))
            assertEquals(
                1L,
                db.selectLong("SELECT value FROM boolean_preference WHERE key = 'black_theme'")
            )
        }
    }

    @Test
    fun `migrating 2 to 3 leaves stats cascading from their topic`() {
        helper.createDatabase(2).use { db ->
            db.execSQL(
                """
                INSERT INTO stat (date, focusTimeQ1, focusTimeQ2, focusTimeQ3, focusTimeQ4, breakTime)
                VALUES ('2026-03-12', 1, 2, 3, 4, 5)
                """
            )
        }

        helper.runMigrationsAndValidate(3, listOf(MIGRATION_2_3)).use { db ->
            db.execSQL("PRAGMA foreign_keys = ON")
            db.execSQL("DELETE FROM topic WHERE id = $DEFAULT_TOPIC_ID")

            assertEquals(0L, db.selectLong("SELECT COUNT(*) FROM stat"))
        }
    }

    @Test
    fun `migrating 2 to 3 keeps topic names unique case-insensitively`() {
        helper.createDatabase(2).close()

        helper.runMigrationsAndValidate(3, listOf(MIGRATION_2_3)).use { db ->
            db.execSQL(
                """
                INSERT INTO topic
                    (name, color, shape, focusTime, shortBreakTime, longBreakTime, sessionLength,
                     autostartNextSession, dndEnabled)
                VALUES ('Reading', 0, 'CIRCLE', 1, 1, 1, 1, 0, 0)
                """
            )

            assertTrue(
                runCatching {
                    db.execSQL(
                        """
                        INSERT INTO topic
                            (name, color, shape, focusTime, shortBreakTime, longBreakTime, sessionLength,
                             autostartNextSession, dndEnabled)
                        VALUES ('reading', 0, 'CIRCLE', 1, 1, 1, 1, 0, 0)
                        """
                    )
                }.isFailure
            )

            assertFalse(
                runCatching {
                    db.execSQL(
                        """
                        INSERT INTO topic
                            (name, color, shape, focusTime, shortBreakTime, longBreakTime, sessionLength,
                             autostartNextSession, dndEnabled)
                        VALUES ('Writing', 0, 'CIRCLE', 1, 1, 1, 1, 0, 0)
                        """
                    )
                }.isFailure
            )
        }
    }
}

private fun SQLiteConnection.selectLong(sql: String): Long =
    prepare(sql).use {
        it.step()
        it.getLong(0)
    }

private fun SQLiteConnection.selectRow(sql: String): List<Any?> =
    prepare(sql).use { statement ->
        assertTrue(statement.step(), "expected a row for: $sql")
        List(statement.getColumnCount()) { column ->
            when (statement.getColumnType(column)) {
                SQLITE_DATA_TEXT -> statement.getText(column)
                SQLITE_DATA_NULL -> null
                else -> statement.getLong(column)
            }
        }
    }
