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

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.databasesDir
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.create
import org.koin.plugin.module.dsl.single
import org.koin.plugin.module.dsl.viewModel
import org.nsh07.pomodoro.BuildKonfig
import org.nsh07.pomodoro.billing.BillingManager
import org.nsh07.pomodoro.data.AppDatabase
import org.nsh07.pomodoro.data.AppPreferenceRepository
import org.nsh07.pomodoro.data.AppStatRepository
import org.nsh07.pomodoro.data.AppTopicRepository
import org.nsh07.pomodoro.data.BackupRestoreManager
import org.nsh07.pomodoro.data.DesktopBackupRestoreManager
import org.nsh07.pomodoro.data.MIGRATION_2_3
import org.nsh07.pomodoro.data.PreferenceRepository
import org.nsh07.pomodoro.data.StatRepository
import org.nsh07.pomodoro.data.StateRepository
import org.nsh07.pomodoro.data.Topic.Companion.defaultTopic
import org.nsh07.pomodoro.data.TopicRepository
import org.nsh07.pomodoro.service.TimerHelper
import org.nsh07.pomodoro.service.TimerManager
import org.nsh07.pomodoro.timer.DesktopTimerHelper
import org.nsh07.pomodoro.ui.settingsScreen.screens.PlatformSettingsViewModel
import org.nsh07.pomodoro.ui.settingsScreen.screens.backupRestore.viewModel.BackupRestoreViewModel
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsViewModel
import org.nsh07.pomodoro.ui.statsScreen.viewModel.StatsViewModel
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerViewModel
import java.io.File

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
    viewModel<PlatformSettingsViewModel>()
}

val desktopModule = module {
    single<DesktopBackupRestoreManager>() bind BackupRestoreManager::class
    single<WindowState> { create(::createWindowState) }
}

val servicesModule = module {
    single<CoroutineDispatcher> { Dispatchers.IO }

    single<AppInfo> { create(::createAppInfo) }
    single<AppStatRepository>() bind StatRepository::class
    single<AppTopicRepository>() bind TopicRepository::class
    single<AppPreferenceRepository>() bind PreferenceRepository::class
    single<StateRepository>()
    single<DesktopTimerHelper>() bind TimerHelper::class
    single<TimerManager> { TimerManager(get(), get(), { System.nanoTime() / 1_000_000L }) }

    single<ActivityCallbacks>()
}

val flavorModule = module {
    single<BillingManager> { FossBillingManager() }
}

val flavorUiModule = module {
    single {
        FlavorUI(
            tomatoPlusPaywallDialog = ::TomatoPlusPaywallDialog,
            topButton = ::TopButton,
            bottomButton = ::BottomButton
        )
    }
}

private fun createDatabase(): AppDatabase {
    val dbFile = File(FileKit.databasesDir.path, BuildKonfig.DATABASE_NAME)
    return Room
        .databaseBuilder<AppDatabase>(name = dbFile.absolutePath)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .addMigrations(MIGRATION_2_3)
        .addCallback(object : RoomDatabase.Callback() {
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
        })
        .build()
}

private fun createAppInfo(): AppInfo {
    return AppInfo(BuildKonfig.DEBUG)
}

private fun createWindowState(): WindowState {
    return WindowState(
        position = WindowPosition.Aligned(alignment = Alignment.Center),
        size = DpSize(1000.dp, 650.dp)
    )
}
