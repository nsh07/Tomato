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

package org.nsh07.pomodoro.ui.settingsScreen

import android.Manifest
import android.content.Context
import android.content.Context.VIBRATOR_MANAGER_SERVICE
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

class AndroidPlatformVibrator(context: Context) : PlatformVibrator {
    private val vibrator =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

    override val hasVibrator: Boolean = vibrator.hasVibrator()

    override val hasAmplitudeControl: Boolean = vibrator.hasAmplitudeControl()

    @RequiresPermission(Manifest.permission.VIBRATE)
    override fun playWaveform(onDuration: Long, offDuration: Long, amplitude: Int) {
        if (!hasVibrator) return

        val timings = longArrayOf(0, onDuration, offDuration, onDuration)
        val amplitudes = intArrayOf(0, amplitude, 0, amplitude)

        val effect = VibrationEffect.createWaveform(timings, amplitudes, 2)

        vibrator.vibrate(effect)
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    override fun cancel() {
        vibrator.cancel()
    }
}

@RequiresPermission(Manifest.permission.VIBRATE)
@Composable
actual fun rememberPlatformVibrator(): PlatformVibrator {
    val context = LocalContext.current
    val vibrator = remember { AndroidPlatformVibrator(context) }

    DisposableEffect(vibrator) {
        onDispose { vibrator.cancel() }
    }
    return vibrator
}