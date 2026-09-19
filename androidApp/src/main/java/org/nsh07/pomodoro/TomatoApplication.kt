/*
 * Copyright (c) 2025-2026 Nishant Mishra
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

package org.nsh07.pomodoro

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.service.quicksettings.TileService
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.nsh07.pomodoro.billing.initializePurchases
import org.nsh07.pomodoro.di.androidModule
import org.nsh07.pomodoro.di.dbModule
import org.nsh07.pomodoro.di.servicesModule
import org.nsh07.pomodoro.di.viewModels
import org.nsh07.pomodoro.qsTile.TomatoQSTileService
import org.nsh07.pomodoro.service.TimerManager

class TomatoApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initializePurchases(this)

        val notificationChannel = NotificationChannel(
            "timer",
            getString(R.string.timer_progress),
            NotificationManager.IMPORTANCE_DEFAULT
        )


        startKoin {
            androidLogger(Level.INFO)

            androidContext(this@TomatoApplication)
            modules(
                dbModule,
                flavorModule,
                flavorUiModule,
                servicesModule,
                androidModule,
                viewModels
            )
        }

        get<NotificationManagerCompat>().createNotificationChannel(notificationChannel)

        // created eagerly so that a stored session is restored however the process was started
        val timerManager = get<TimerManager>()

        // The tile keeps whatever it last showed, from before a reboot or a kill, until told to
        // ask again, and asking before the restore has run would only show it a fresh timer
        CoroutineScope(Dispatchers.Default).launch {
            timerManager.awaitRestore()
            TileService.requestListeningState(
                this@TomatoApplication,
                ComponentName(this@TomatoApplication, TomatoQSTileService::class.java)
            )
        }
    }
}
