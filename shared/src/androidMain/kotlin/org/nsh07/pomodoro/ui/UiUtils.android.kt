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

package org.nsh07.pomodoro.ui

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.Density
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsAction
import org.nsh07.pomodoro.utils.androidSdkVersionAtLeast
import tomato.shared.generated.resources.Res
import tomato.shared.generated.resources.alarm_sound
import tomato.shared.generated.resources.app_name
import tomato.shared.generated.resources.dnd_permission_message

@Composable
actual fun AodSystemBarsHandler(
    density: Density,
    windowInfo: WindowInfo,
    secureAod: Boolean,
    setTimerFrequency: (Float) -> Unit
) {
    val activity = LocalActivity.current
    val view = LocalView.current

    val window = remember { (view.context as Activity).window }
    val insetsController = remember { WindowCompat.getInsetsController(window, view) }

    DisposableEffect(Unit) {
        setTimerFrequency(1f)
        window.addFlags(
            if (secureAod) {
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            } else WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            activity?.setShowWhenLocked(true)
        }
        insetsController.apply {
            hide(WindowInsetsCompat.Type.statusBars())
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        onDispose {
            setTimerFrequency(60f)
            window.clearFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                activity?.setShowWhenLocked(false)
            }
            insetsController.apply {
                show(WindowInsetsCompat.Type.statusBars())
                show(WindowInsetsCompat.Type.navigationBars())
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
            }
        }
    }
}

@Composable
actual fun rememberRequestDndPermissionCallback(): (Boolean) -> Unit {
    val context = LocalContext.current
    val inspectionMode = LocalInspectionMode.current

    val permissionString =
        stringResource(Res.string.dnd_permission_message, stringResource(Res.string.app_name))

    return remember {
        val notificationManagerService = if (!inspectionMode)
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        else null

        { dndEnabled ->
            if (dndEnabled && notificationManagerService?.isNotificationPolicyAccessGranted() == false) {
                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)

                Toast
                    .makeText(
                        context,
                        permissionString,
                        Toast.LENGTH_LONG
                    )
                    .show()

                context.startActivity(intent)
            } else if (!dndEnabled && notificationManagerService?.isNotificationPolicyAccessGranted() == true) {
                notificationManagerService.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
actual fun rememberRequestNotificationPermissionCallback(): () -> Unit {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {}
    )
    return { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
}

@Composable
actual fun rememberRingtonePickerLauncherCallback(
    alarmSoundFilePath: String?,
    onResult: (SettingsAction) -> Unit
): () -> Unit {
    val alamSoundString = stringResource(Res.string.alarm_sound)

    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri =
                if (androidSdkVersionAtLeast(33)) {
                    result.data?.getParcelableExtra(
                        RingtoneManager.EXTRA_RINGTONE_PICKED_URI,
                        Uri::class.java
                    )
                } else {
                    @Suppress("DEPRECATION")
                    result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                }
            onResult(SettingsAction.SaveAlarmSound(uri.toString()))
        }
    }

    val ringtonePickerIntent = remember(alarmSoundFilePath) {
        Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, alamSoundString)
            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, alarmSoundFilePath?.toUri())
        }
    }

    return { ringtonePickerLauncher.launch(ringtonePickerIntent) }
}

@Composable
actual fun rememberRingtoneNameProviderCallback(): suspend (String?) -> String {
    val context = LocalContext.current

    return remember {
        { alarmSoundFilePath ->
            withContext(Dispatchers.IO) {
                try {
                    RingtoneManager.getRingtone(context, alarmSoundFilePath?.toUri())
                        ?.getTitle(context) ?: "..."
                } catch (e: Exception) {
                    Log.e("AlarmSettings", "Unable to get ringtone title: ${e.message}")
                    e.printStackTrace()
                    "..."
                }
            }
        }
    }
}

actual fun Modifier.androidSystemGestureExclusion() = this.systemGestureExclusion()

actual fun htmlToAnnotatedString(html: String): AnnotatedString = AnnotatedString.fromHtml(html)