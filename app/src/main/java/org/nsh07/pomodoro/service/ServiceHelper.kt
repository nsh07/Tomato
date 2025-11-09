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

package org.nsh07.pomodoro.service

import android.content.Context
import android.content.Intent
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerAction

/**
 * Helper class that holds a reference to [Context] and helps call [Context.startService] in
 * [androidx.lifecycle.ViewModel]s. This class must be managed by an [android.app.Application] class
 * to scope it to the Activity's lifecycle and prevent leaks.
 */
class ServiceHelper(private val context: Context) {
    fun startService(action: TimerAction) {
        when (action) {
            TimerAction.ResetTimer ->
                Intent(context, TimerService::class.java).also {
                    it.action = TimerService.Actions.RESET.toString()
                    context.startService(it)
                }

            is TimerAction.SkipTimer ->
                Intent(context, TimerService::class.java).also {
                    it.action = TimerService.Actions.SKIP.toString()
                    context.startService(it)
                }

            TimerAction.StopAlarm ->
                Intent(context, TimerService::class.java).also {
                    it.action =
                        TimerService.Actions.STOP_ALARM.toString()
                    context.startService(it)
                }

            TimerAction.ToggleTimer ->
                Intent(context, TimerService::class.java).also {
                    it.action = TimerService.Actions.TOGGLE.toString()
                    context.startService(it)
                }
        }
    }
}