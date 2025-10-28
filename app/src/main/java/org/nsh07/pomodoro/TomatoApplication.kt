/*
 * Copyright (c) 2025 Nishant Mishra
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
import org.nsh07.pomodoro.billing.initializePurchases
import org.nsh07.pomodoro.data.AppContainer
import org.nsh07.pomodoro.data.DefaultAppContainer

class TomatoApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)

        initializePurchases(this)

        val notificationChannel = NotificationChannel(
            "timer",
            getString(R.string.timer_progress),
            NotificationManager.IMPORTANCE_DEFAULT
        )

        container.notificationManager.createNotificationChannel(notificationChannel)
    }
}