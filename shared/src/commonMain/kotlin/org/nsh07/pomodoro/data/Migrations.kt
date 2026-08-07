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

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import kotlinx.coroutines.runBlocking

class Migration2to3(
    private val getDeviceId: suspend () -> String
) : Migration(2, 3) {
    override fun migrate(connection: SQLiteConnection) {
        val currentTimeMillis = System.currentTimeMillis()
        val currentDeviceId =
            runBlocking { getDeviceId() } // must run blocking to get correct device id from disk

        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `stat_new` (
                `date` TEXT NOT NULL, 
                `deviceId` TEXT NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                `focusTimeQ1` INTEGER NOT NULL,
                `focusTimeQ2` INTEGER NOT NULL,
                `focusTimeQ3` INTEGER NOT NULL,
                `focusTimeQ4` INTEGER NOT NULL,
                `breakTime` INTEGER NOT NULL,
                PRIMARY KEY(`date`, `deviceId`)
            )
            """.trimIndent()
        )

        connection.execSQL(
            """
            INSERT INTO `stat_new` (
                `date`, `deviceId`, `updatedAt`, `focusTimeQ1`, `focusTimeQ2`, 
                `focusTimeQ3`, `focusTimeQ4`, `breakTime`
            )
            SELECT 
                `date`, '$currentDeviceId', $currentTimeMillis, `focusTimeQ1`, `focusTimeQ2`, 
                `focusTimeQ3`, `focusTimeQ4`, `breakTime` 
            FROM `stat`
            """.trimIndent()
        )

        connection.execSQL("DROP TABLE `stat`")
        connection.execSQL("ALTER TABLE `stat_new` RENAME TO `stat`")
    }
}