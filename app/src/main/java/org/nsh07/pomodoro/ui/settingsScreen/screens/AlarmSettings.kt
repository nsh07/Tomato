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

package org.nsh07.pomodoro.ui.settingsScreen.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.ui.settingsScreen.SettingsSwitchItem
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsAction
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsState
import org.nsh07.pomodoro.ui.theme.AppFonts.robotoFlexTopBar
import org.nsh07.pomodoro.ui.theme.CustomColors.listItemColors
import org.nsh07.pomodoro.ui.theme.CustomColors.switchColors
import org.nsh07.pomodoro.ui.theme.CustomColors.topBarColors
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.bottomListItemShape
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.middleListItemShape
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.topListItemShape

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AlarmSettings(
    settingsState: SettingsState,
    onAction: (SettingsAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val context = LocalContext.current

    var alarmName by remember { mutableStateOf("...") }

    LaunchedEffect(settingsState.alarmSound) {
        withContext(Dispatchers.IO) {
            alarmName =
                RingtoneManager.getRingtone(context, settingsState.alarmSound.toUri())
                    ?.getTitle(context) ?: ""
        }
    }

    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    result.data?.getParcelableExtra(
                        RingtoneManager.EXTRA_RINGTONE_PICKED_URI,
                        Uri::class.java
                    )
                } else {
                    @Suppress("DEPRECATION")
                    result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                }
            onAction(SettingsAction.SaveAlarmSound(uri))
        }
    }

    @SuppressLint("LocalContextGetResourceValueCall")
    val ringtonePickerIntent = remember(settingsState.alarmSound) {
        Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, context.getString(R.string.alarm_sound))
            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, settingsState.alarmSound.toUri())
        }
    }

    val switchItems = remember(
        settingsState.blackTheme,
        settingsState.aodEnabled,
        settingsState.alarmEnabled,
        settingsState.vibrateEnabled
    ) {
        listOf(
            SettingsSwitchItem(
                checked = settingsState.alarmEnabled,
                icon = R.drawable.alarm_on,
                label = R.string.sound,
                description = R.string.alarm_desc,
                onClick = { onAction(SettingsAction.SaveAlarmEnabled(it)) }
            ),
            SettingsSwitchItem(
                checked = settingsState.vibrateEnabled,
                icon = R.drawable.mobile_vibrate,
                label = R.string.vibrate,
                description = R.string.vibrate_desc,
                onClick = { onAction(SettingsAction.SaveVibrateEnabled(it)) }
            )
        )
    }

    Column(modifier.nestedScroll(scrollBehavior.nestedScrollConnection)) {
        LargeFlexibleTopAppBar(
            title = {
                Text(stringResource(R.string.alarm), fontFamily = robotoFlexTopBar)
            },
            subtitle = {
                Text(stringResource(R.string.settings))
            },
            navigationIcon = {
                IconButton(onBack) {
                    Icon(
                        painterResource(R.drawable.arrow_back),
                        null
                    )
                }
            },
            colors = topBarColors,
            scrollBehavior = scrollBehavior
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                .background(topBarColors.containerColor)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(Modifier.height(14.dp))
            }

            item {
                ListItem(
                    leadingContent = {
                        Icon(painterResource(R.drawable.alarm), null)
                    },
                    headlineContent = { Text(stringResource(R.string.alarm_sound)) },
                    supportingContent = { Text(alarmName) },
                    colors = listItemColors,
                    modifier = Modifier
                        .clip(topListItemShape)
                        .clickable(onClick = { ringtonePickerLauncher.launch(ringtonePickerIntent) })
                )
            }
            itemsIndexed(switchItems) { index, item ->
                ListItem(
                    leadingContent = {
                        Icon(painterResource(item.icon), contentDescription = null)
                    },
                    headlineContent = { Text(stringResource(item.label)) },
                    supportingContent = { Text(stringResource(item.description)) },
                    trailingContent = {
                        Switch(
                            checked = item.checked,
                            onCheckedChange = { item.onClick(it) },
                            thumbContent = {
                                if (item.checked) {
                                    Icon(
                                        painter = painterResource(R.drawable.check),
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(R.drawable.clear),
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                }
                            },
                            colors = switchColors
                        )
                    },
                    colors = listItemColors,
                    modifier = Modifier
                        .clip(
                            when (index) {
                                switchItems.lastIndex -> bottomListItemShape
                                else -> middleListItemShape
                            }
                        )
                )
            }

            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Preview
@Composable
fun AlarmSettingsPreview() {
    val settingsState = SettingsState()
    AlarmSettings(
        settingsState = settingsState,
        onAction = {},
        onBack = {}
    )
}
