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

package org.nsh07.pomodoro.service

import android.content.Context
import android.content.Intent
import android.util.Log
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerAction

/**
 * Helper class that holds a reference to [Context] and helps call
 * [Context.startForegroundService] in
 * [androidx.lifecycle.ViewModel]s. This class must be managed by an [android.app.Application] class
 * to scope it to the Activity's lifecycle and prevent leaks.
 */
class AndroidTimerHelper(private val context: Context) : TimerHelper {
    override fun onAction(action: TimerAction) {
        val serviceAction = when (action) {
            TimerAction.ResetTimer -> TimerService.Actions.RESET
            TimerAction.UndoReset -> TimerService.Actions.UNDO_RESET
            is TimerAction.SkipTimer -> TimerService.Actions.SKIP
            TimerAction.StopAlarm -> TimerService.Actions.STOP_ALARM
            TimerAction.ToggleTimer -> TimerService.Actions.TOGGLE
            else -> {
                Log.e("StartService", "Invalid action: $action")
                return
            }
        }

        try {
            Intent(context, TimerService::class.java).also {
                it.action = serviceAction.toString()
                context.startForegroundService(it)
            }
        } catch (e: Exception) {
            Log.e("StartService", "Cannot start service with action $action: ${e.message}")
            e.printStackTrace()
        }
    }
}