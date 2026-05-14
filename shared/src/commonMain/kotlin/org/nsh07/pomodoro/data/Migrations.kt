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
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `topic` (
                `id` TEXT NOT NULL, 
                `name` TEXT NOT NULL, 
                `color` INTEGER NOT NULL, 
                `focusTime` INTEGER NOT NULL, 
                `shortBreakTime` INTEGER NOT NULL, 
                `longBreakTime` INTEGER NOT NULL, 
                `sessionLength` INTEGER NOT NULL, 
                `autoStartNextSession` INTEGER NOT NULL, 
                `dndEnabled` INTEGER NOT NULL, 
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )

        // Seed default topic with current global settings or hardcoded defaults if preferences don't exist
        connection.execSQL(
            """
            INSERT OR IGNORE INTO `topic` 
                (`id`, `name`, `color`, `focusTime`, `shortBreakTime`, `longBreakTime`, `sessionLength`, `autoStartNextSession`, `dndEnabled`)
            VALUES (
                'default', 
                'Default', 
                ${Color.White.value.toLong()}, 
                COALESCE((SELECT value FROM int_preference WHERE key = 'focus_time'), 1500000),
                COALESCE((SELECT value FROM int_preference WHERE key = 'short_break_time'), 300000),
                COALESCE((SELECT value FROM int_preference WHERE key = 'long_break_time'), 900000),
                COALESCE((SELECT value FROM int_preference WHERE key = 'session_length'), 4),
                COALESCE((SELECT value FROM boolean_preference WHERE key = 'autostart_next_session'), 0),
                COALESCE((SELECT value FROM boolean_preference WHERE key = 'dnd_enabled'), 0)
            )
            """.trimIndent()
        )

        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `new_stat` (
                `date` TEXT NOT NULL, 
                `topicId` TEXT NOT NULL, 
                `focusTimeQ1` INTEGER NOT NULL, 
                `focusTimeQ2` INTEGER NOT NULL, 
                `focusTimeQ3` INTEGER NOT NULL, 
                `focusTimeQ4` INTEGER NOT NULL, 
                `breakTime` INTEGER NOT NULL, 
                PRIMARY KEY(`date`, `topicId`), 
                FOREIGN KEY(`topicId`) REFERENCES `topic`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )

        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_stat_topicId` ON `new_stat` (`topicId`)")

        // Copy existing data into new_stat, mapping to 'default' topic
        connection.execSQL(
            """
            INSERT INTO `new_stat` (`date`, `topicId`, `focusTimeQ1`, `focusTimeQ2`, `focusTimeQ3`, `focusTimeQ4`, `breakTime`)
            SELECT `date`, 'default', `focusTimeQ1`, `focusTimeQ2`, `focusTimeQ3`, `focusTimeQ4`, `breakTime` FROM `stat`
            """.trimIndent()
        )

        connection.execSQL("DROP TABLE `stat`")
        connection.execSQL("ALTER TABLE `new_stat` RENAME TO `stat`")

        // Clean up migrated settings from global preference tables
        connection.execSQL("DELETE FROM int_preference WHERE key IN ('focus_time', 'short_break_time', 'long_break_time', 'session_length')")
        connection.execSQL("DELETE FROM boolean_preference WHERE key IN ('autostart_next_session', 'dnd_enabled')")
    }
}
