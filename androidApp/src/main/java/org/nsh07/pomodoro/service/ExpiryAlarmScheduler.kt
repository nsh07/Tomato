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

package org.nsh07.pomodoro.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

/**
 * Wakes the device when the running interval is due to end.
 */
class ExpiryAlarmScheduler(private val context: Context) {

    private val alarmManager by lazy { context.getSystemService(AlarmManager::class.java) }

    // Reused, so that setting a new alarm replaces the pending one
    private val pendingIntent by lazy {
        PendingIntent.getService(
            context,
            0,
            Intent(context, TimerService::class.java)
                .setAction(TimerService.Actions.EXPIRE.toString()),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /** Schedules a wakeup at [triggerAtElapsedRealtime], or cancels the pending one when null. */
    fun set(triggerAtElapsedRealtime: Long?) {
        if (triggerAtElapsedRealtime == null) {
            alarmManager.cancel(pendingIntent)
            return
        }

        try {
            if (canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtElapsedRealtime, pendingIntent
                )
            } else {
                setInexactAlarm(triggerAtElapsedRealtime)
            }
        } catch (e: SecurityException) {
            // The exact alarm permission can be revoked between the check and the call
            Log.e("ExpiryAlarmScheduler", "Cannot set exact alarm: ${e.message}")
            setInexactAlarm(triggerAtElapsedRealtime)
        }
    }

    /** Doze batches inexact alarms to roughly one every nine minutes, so this can end late. */
    private fun setInexactAlarm(triggerAtElapsedRealtime: Long) =
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtElapsedRealtime, pendingIntent
        )

    private fun canScheduleExactAlarms() =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
}
