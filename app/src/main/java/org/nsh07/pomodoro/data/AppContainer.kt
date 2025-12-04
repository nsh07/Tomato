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

package org.nsh07.pomodoro.data

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.flow.MutableStateFlow
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.billing.BillingManager
import org.nsh07.pomodoro.billing.BillingManagerProvider
import org.nsh07.pomodoro.service.ServiceHelper
import org.nsh07.pomodoro.service.addTimerActions

interface AppContainer {
    val appPreferenceRepository: AppPreferenceRepository
    val appStatRepository: AppStatRepository
    val stateRepository: StateRepository
    val billingManager: BillingManager
    val notificationManager: NotificationManagerCompat
    val notificationManagerService: NotificationManager
    val notificationBuilder: NotificationCompat.Builder
    val serviceHelper: ServiceHelper
    val time: MutableStateFlow<Long>
    var activityTurnScreenOn: (Boolean) -> Unit
}

class DefaultAppContainer(context: Context) : AppContainer {

    override val appPreferenceRepository: AppPreferenceRepository by lazy {
        AppPreferenceRepository(AppDatabase.getDatabase(context).preferenceDao())
    }

    override val appStatRepository: AppStatRepository by lazy {
        AppStatRepository(AppDatabase.getDatabase(context).statDao())
    }

    override val stateRepository: StateRepository by lazy {
        StateRepository()
    }

    override val billingManager: BillingManager by lazy { BillingManagerProvider.manager }

    override val notificationManager: NotificationManagerCompat by lazy {
        NotificationManagerCompat.from(context)
    }

    override val notificationManagerService: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override val notificationBuilder: NotificationCompat.Builder by lazy {
        NotificationCompat.Builder(context, "timer")
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
            .addTimerActions(context, R.drawable.play, context.getString(R.string.start))
            .setShowWhen(true)
            .setSilent(true)
            .setOngoing(true)
            .setRequestPromotedOngoing(true)
            .setVisibility(VISIBILITY_PUBLIC)
    }

    override val serviceHelper: ServiceHelper by lazy {
        ServiceHelper(context)
    }

    override val time: MutableStateFlow<Long> by lazy {
        MutableStateFlow(stateRepository.settingsState.value.focusTime)
    }

    override var activityTurnScreenOn: (Boolean) -> Unit = {}
}