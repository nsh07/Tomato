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

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.os.SystemClock
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.create
import org.koin.plugin.module.dsl.single
import org.nsh07.pomodoro.BuildConfig
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.data.AppPreferenceRepository
import org.nsh07.pomodoro.data.AppStatRepository
import org.nsh07.pomodoro.data.PreferenceRepository
import org.nsh07.pomodoro.data.StatRepository
import org.nsh07.pomodoro.data.StateRepository
import org.nsh07.pomodoro.service.AndroidTimerHelper
import org.nsh07.pomodoro.service.TimerHelper
import org.nsh07.pomodoro.service.TimerManager
import org.nsh07.pomodoro.service.addTimerActions

val servicesModule = module {
    single<CoroutineDispatcher> { Dispatchers.IO }

    single<AppInfo> { create(::createAppInfo) }
    single<AppStatRepository>() bind StatRepository::class
    single<AppPreferenceRepository>() bind PreferenceRepository::class
    single<StateRepository>()
    single<AndroidTimerHelper>() bind TimerHelper::class
    single<TimerManager> { TimerManager(get(), get(), SystemClock::elapsedRealtime) }

    single { NotificationManagerCompat.from(get()) }
    single { create(::createNotificationManager) }
    single { create(::createNotificationCompatBuilder) }

    single<ActivityCallbacks>()
}

private fun createAppInfo(): AppInfo = AppInfo(BuildConfig.DEBUG)

private fun createNotificationManager(context: Context): NotificationManager {
    return context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
}

private fun createNotificationCompatBuilder(context: Context): NotificationCompat.Builder {
    return NotificationCompat.Builder(context, "timer")
        .setSmallIcon(R.drawable.tomato_logo_notification)
        .setColor(Color.Red.toArgb())
        .setContentIntent(
            PendingIntent.getActivity(
                context,
                0,
                context.packageManager.getLaunchIntentForPackage(context.packageName),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
        .addTimerActions(context, context.getString(R.string.start))
        .setShowWhen(true)
        .setSilent(true)
        .setOngoing(true)
        .setRequestPromotedOngoing(true)
        .setVisibility(VISIBILITY_PUBLIC)
        .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
}
