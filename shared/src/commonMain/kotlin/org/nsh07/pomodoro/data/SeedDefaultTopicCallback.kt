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

import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import org.nsh07.pomodoro.data.Topic.Companion.defaultTopic

/**
 * Writes [defaultTopic] into a freshly created database
 */
object SeedDefaultTopicCallback : RoomDatabase.Callback() {
    override fun onCreate(connection: SQLiteConnection) {
        super.onCreate(connection)
        connection.execSQL(
            """
            INSERT OR IGNORE INTO `topic` 
                (`id`, `name`, `color`, `shape`, `focusTime`, `shortBreakTime`, `longBreakTime`, `sessionLength`, `autostartNextSession`, `dndEnabled`)
            VALUES (
                ${defaultTopic.id}, 
                '${defaultTopic.name}', 
                ${defaultTopic.color.value.toLong()},
                '${defaultTopic.shape.name}',
                ${defaultTopic.focusTime},
                ${defaultTopic.shortBreakTime},
                ${defaultTopic.longBreakTime},
                ${defaultTopic.sessionLength},
                ${if (defaultTopic.autostartNextSession) 1 else 0},
                ${if (defaultTopic.dndEnabled) 1 else 0}
            )
            """.trimIndent()
        )
    }
}
