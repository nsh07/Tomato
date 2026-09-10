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

package org.nsh07.pomodoro.reporting

import android.content.Context
import androidx.core.content.edit
import org.nsh07.pomodoro.BuildConfig
import sh.measure.android.Measure
import sh.measure.android.config.MeasureConfig

private const val PREFS_NAME = "reporting"
private const val KEY_ENABLED = "crash_reporting_enabled"
private const val KEY_NOTICE_SHOWN = "crash_reporting_notice_shown"

/**
 * Opt-in state for Measure crash/ANR reporting. Reporting stays off until the user allows it in the
 * notice shown on first launch, and [initMeasureReporting] does not initialize the SDK before that.
 */
object CrashReporting {
    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isEnabled(context: Context): Boolean = prefs(context).getBoolean(KEY_ENABLED, false)

    fun setEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit { putBoolean(KEY_ENABLED, enabled) }
        if (enabled) {
            Measure.init(context, MeasureConfig(enableLogging = BuildConfig.DEBUG))
            Measure.start()
        } else {
            Measure.stop()
        }
    }

    fun isNoticeShown(context: Context): Boolean =
        prefs(context).getBoolean(KEY_NOTICE_SHOWN, false)

    fun setNoticeShown(context: Context) {
        prefs(context).edit { putBoolean(KEY_NOTICE_SHOWN, true) }
    }
}
