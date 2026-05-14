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

package org.nsh07.pomodoro.di

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.create
import org.koin.plugin.module.dsl.single
import org.koin.plugin.module.dsl.viewModel
import org.nsh07.pomodoro.BuildKonfig
import org.nsh07.pomodoro.data.AndroidBackupRestoreManager
import org.nsh07.pomodoro.data.AppDatabase
import org.nsh07.pomodoro.data.AppTopicRepository
import org.nsh07.pomodoro.data.BackupRestoreManager
import org.nsh07.pomodoro.data.MIGRATION_2_3
import org.nsh07.pomodoro.data.TopicRepository
import org.nsh07.pomodoro.ui.settingsScreen.screens.backupRestore.viewModel.BackupRestoreViewModel
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsViewModel
import org.nsh07.pomodoro.ui.statsScreen.viewModel.StatsViewModel
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerViewModel

val dbModule = module {
    single<AppDatabase> { create(::createDatabase) }
    single { get<AppDatabase>().preferenceDao() }
    single { get<AppDatabase>().statDao() }
    single { get<AppDatabase>().topicDao() }
    single { get<AppDatabase>().systemDao() }
}

val viewModels = module {
    viewModel<BackupRestoreViewModel>()
    viewModel<TimerViewModel>()
    viewModel<SettingsViewModel>()
    viewModel<StatsViewModel>()
}

val androidModule = module {
    single<AndroidBackupRestoreManager>() bind BackupRestoreManager::class
    single<AppTopicRepository>() bind TopicRepository::class
}

private fun createDatabase(context: Context): AppDatabase {
    return Room
        .databaseBuilder(
            context,
            AppDatabase::class.java,
            BuildKonfig.DATABASE_NAME
        )
        .addMigrations(MIGRATION_2_3)
        .addCallback(
            object : RoomDatabase.Callback() {
                override fun onCreate(connection: SQLiteConnection) {
                    super.onCreate(connection)
                    connection.execSQL(
                        """
                        INSERT OR IGNORE INTO `topic` 
                            (`id`, `name`, `color`, `focusTime`, `shortBreakTime`, `longBreakTime`, `sessionLength`, `autoStartNextSession`, `dndEnabled`)
                        VALUES (
                            'default', 
                            'Default', 
                            ${Color.White.value.toLong()}, 
                            (SELECT value FROM int_preference WHERE key = 'focus_time'),
                            (SELECT value FROM int_preference WHERE key = 'short_break_time'),
                            (SELECT value FROM int_preference WHERE key = 'long_break_time'),
                            (SELECT value FROM int_preference WHERE key = 'session_length'),
                            (SELECT value FROM boolean_preference WHERE key = 'autostart_next_session'),
                            (SELECT value FROM boolean_preference WHERE key = 'dnd_enabled')
                        )
                        """.trimIndent()
                    )
                    (-4294967296L).toULong()
                }
            }
        )
        .build()
}
