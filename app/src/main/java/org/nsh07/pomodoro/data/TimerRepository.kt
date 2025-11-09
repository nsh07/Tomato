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

import android.net.Uri
import android.provider.Settings
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Interface that holds the timer durations for each timer type. This repository maintains a single
 * source of truth for the timer durations for the various ViewModels in the app.
 */
interface TimerRepository {
    var focusTime: Long
    var shortBreakTime: Long
    var longBreakTime: Long

    var sessionLength: Int

    var timerFrequency: Float

    var alarmEnabled: Boolean
    var vibrateEnabled: Boolean
    var dndEnabled: Boolean

    var colorScheme: ColorScheme

    var alarmSoundUri: Uri?

    var serviceRunning: MutableStateFlow<Boolean>
}

/**
 * See [TimerRepository] for more details
 */
class AppTimerRepository : TimerRepository {
    override var focusTime = 25 * 60 * 1000L
    override var shortBreakTime = 5 * 60 * 1000L
    override var longBreakTime = 15 * 60 * 1000L
    override var sessionLength = 4
    override var timerFrequency: Float = 60f
    override var alarmEnabled = true
    override var vibrateEnabled = true
    override var dndEnabled: Boolean = false
    override var colorScheme = lightColorScheme()
    override var alarmSoundUri: Uri? =
        Settings.System.DEFAULT_ALARM_ALERT_URI ?: Settings.System.DEFAULT_RINGTONE_URI
    override var serviceRunning = MutableStateFlow(false)
}