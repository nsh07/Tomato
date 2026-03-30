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

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
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
import org.nsh07.pomodoro.billing.BillingManager
import org.nsh07.pomodoro.billing.FossBillingManager
import org.nsh07.pomodoro.billing.TomatoPlusPaywallDialog
import org.nsh07.pomodoro.data.AppDatabase
import org.nsh07.pomodoro.data.AppPreferenceRepository
import org.nsh07.pomodoro.data.AppStatRepository
import org.nsh07.pomodoro.data.BackupRestoreManager
import org.nsh07.pomodoro.data.DesktopBackupRestoreManager
import org.nsh07.pomodoro.data.PreferenceRepository
import org.nsh07.pomodoro.data.StatRepository
import org.nsh07.pomodoro.data.StateRepository
import org.nsh07.pomodoro.service.TimerHelper
import org.nsh07.pomodoro.service.TimerManager
import org.nsh07.pomodoro.timer.DesktopTimerHelper
import org.nsh07.pomodoro.ui.settingsScreen.components.BottomButton
import org.nsh07.pomodoro.ui.settingsScreen.components.TopButton
import org.nsh07.pomodoro.ui.settingsScreen.screens.backupRestore.viewModel.BackupRestoreViewModel
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsViewModel
import org.nsh07.pomodoro.ui.statsScreen.viewModel.StatsViewModel
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerViewModel
import java.io.File

val dbModule = module {
    single<AppDatabase> { create(::createDatabase) }
    single { get<AppDatabase>().preferenceDao() }
    single { get<AppDatabase>().statDao() }
    single { get<AppDatabase>().systemDao() }
}

val viewModels = module {
    viewModel<BackupRestoreViewModel>()
    viewModel<TimerViewModel>()
    viewModel<SettingsViewModel>()
    viewModel<StatsViewModel>()
}

val desktopModule = module {
    single<DesktopBackupRestoreManager>() bind BackupRestoreManager::class
}

val servicesModule = module {
    single<CoroutineDispatcher> { Dispatchers.IO }

    single<AppInfo> { create(::createAppInfo) }
    single<AppStatRepository>() bind StatRepository::class
    single<AppPreferenceRepository>() bind PreferenceRepository::class
    single<StateRepository>()
    single<DesktopTimerHelper>() bind TimerHelper::class
    single<TimerManager> { TimerManager(get(), get()) { System.nanoTime() / 1_000_000L } }

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
    val dbFile = File(FileKit.databasesDir.path, "app_database")
    return Room
        .databaseBuilder<AppDatabase>(name = dbFile.absolutePath)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}

private fun createAppInfo(): AppInfo {
    // TODO: Implement AppInfo
    return AppInfo(
        debug = true,
        versionName = "1.0",
        versionCode = 1
    )
}
