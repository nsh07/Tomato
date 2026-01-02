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

package org.nsh07.pomodoro.ui.settingsScreen.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.ui.mergePaddingValues
import org.nsh07.pomodoro.ui.settingsScreen.SettingsSwitchItem
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsAction
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsState
import org.nsh07.pomodoro.ui.theme.AppFonts.robotoFlexTopBar
import org.nsh07.pomodoro.ui.theme.CustomColors.listItemColors
import org.nsh07.pomodoro.ui.theme.CustomColors.switchColors
import org.nsh07.pomodoro.ui.theme.CustomColors.topBarColors
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.bottomListItemShape
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.cardShape
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.middleListItemShape
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.topListItemShape
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AlarmSettings(
    settingsState: SettingsState,
    contentPadding: PaddingValues,
    onAction: (SettingsAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val context = LocalContext.current

    var alarmName by remember { mutableStateOf("...") }

    LaunchedEffect(settingsState.alarmSoundUri) {
        withContext(Dispatchers.IO) {
            alarmName = try {
                RingtoneManager.getRingtone(context, settingsState.alarmSoundUri)
                    ?.getTitle(context) ?: ""
            } catch (e: Exception) {
                Log.e("AlarmSettings", "Unable to get ringtone title: ${e.message}")
                e.printStackTrace()
                ""
            }
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
    val ringtonePickerIntent = remember(settingsState.alarmSoundUri) {
        Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, context.getString(R.string.alarm_sound))
            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, settingsState.alarmSoundUri)
        }
    }

    val switchItems = remember(
        settingsState.alarmEnabled,
        settingsState.vibrateEnabled,
        settingsState.mediaVolumeForAlarm
    ) {
        listOf(
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
            ),
            listOf(
                SettingsSwitchItem(
                    checked = settingsState.mediaVolumeForAlarm,
                    collapsible = true,
                    icon = R.drawable.music_note,
                    label = R.string.media_volume_for_alarm,
                    description = R.string.media_volume_for_alarm_desc,
                    onClick = { onAction(SettingsAction.SaveMediaVolumeForAlarm(it)) }
                )
            )
        )
    }

    Scaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                title = {
                    Text(stringResource(R.string.alarm), fontFamily = robotoFlexTopBar)
                },
                subtitle = {
                    Text(stringResource(R.string.settings))
                },
                navigationIcon = {
                    FilledTonalIconButton(
                        onClick = onBack,
                        shapes = IconButtonDefaults.shapes(),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = listItemColors.containerColor)
                    ) {
                        Icon(
                            painterResource(R.drawable.arrow_back),
                            stringResource(R.string.back)
                        )
                    }
                },
                colors = topBarColors,
                scrollBehavior = scrollBehavior
            )
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        val insets = mergePaddingValues(innerPadding, contentPadding)
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            contentPadding = insets,
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
                    trailingContent = { Icon(painterResource(R.drawable.arrow_forward_big), null) },
                    colors = listItemColors,
                    modifier = Modifier
                        .clip(topListItemShape)
                        .clickable(onClick = { ringtonePickerLauncher.launch(ringtonePickerIntent) })
                )
            }
            switchItems.fastForEach { items ->
                itemsIndexed(items) { index, item ->
                    ListItem(
                        leadingContent = {
                            Icon(painterResource(item.icon), contentDescription = null)
                        },
                        headlineContent = { Text(stringResource(item.label)) },
                        supportingContent = {
                            if (item.collapsible) {
                                var expanded by remember { mutableStateOf(false) }
                                Text(
                                    stringResource(item.description),
                                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .clickable { expanded = !expanded }
                                        .animateContentSize(motionScheme.defaultSpatialSpec())
                                )
                            } else {
                                Text(stringResource(item.description))
                            }
                        },
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
                                when {
                                    items.size == 1 -> cardShape
                                    index == items.lastIndex -> bottomListItemShape
                                    else -> middleListItemShape
                                }
                            )
                    )
                }

                item {
                    Spacer(Modifier.height(12.dp))
                }
            }

            item {
                var vibrationPlaying by remember { mutableStateOf(false) }
                val interactionSources = remember { List(2) { MutableInteractionSource() } }

                ListItem(
                    headlineContent = { Text("Vibration pattern") },
                    trailingContent = {
                        ButtonGroup(
                            overflowIndicator = {},
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            customItem(
                                buttonGroupContent = {
                                    FilledIconToggleButton(
                                        checked = vibrationPlaying,
                                        onCheckedChange = { vibrationPlaying = it },
                                        interactionSource = interactionSources[0],
                                        modifier = Modifier
                                            .size(52.dp, 40.dp)
                                            .animateWidth(interactionSources[0])
                                    ) {
                                        if (vibrationPlaying)
                                            Icon(painterResource(R.drawable.stop), null)
                                        else
                                            Icon(painterResource(R.drawable.play), null)
                                    }
                                },
                                menuContent = {}
                            )
                            customItem(
                                buttonGroupContent = {
                                    FilledTonalIconButton(
                                        onClick = {},
                                        shapes = IconButtonDefaults.shapes(),
                                        interactionSource = interactionSources[1],
                                        modifier = Modifier
                                            .size(40.dp)
                                            .animateWidth(interactionSources[1])
                                    ) {
                                        Icon(painterResource(R.drawable.restore_default), null)
                                    }
                                },
                                menuContent = {}
                            )
                        }
                    },
                    colors = listItemColors,
                    modifier = Modifier.clip(topListItemShape)
                )
            }
            item {
                val sliderState = rememberSliderState(
                    value = 1000f,
                    valueRange = 10f..5000f,
                    onValueChangeFinished = {}
                )

                Column(
                    Modifier
                        .background(listItemColors.containerColor, middleListItemShape)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    ) {
                        Text(stringResource(R.string.on_duration), style = typography.bodyLarge)
                        Spacer(Modifier.weight(1f))
                        Text(
                            "${sliderState.value.roundToInt()}ms",
                            style = typography.labelMedium,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                    Slider(
                        sliderState, modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                    )
                }
            }
            item {
                val sliderState = rememberSliderState(
                    value = 1000f,
                    valueRange = 10f..5000f,
                    onValueChangeFinished = {}
                )

                Column(
                    Modifier
                        .background(listItemColors.containerColor, middleListItemShape)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    ) {
                        Text(stringResource(R.string.off_duration), style = typography.bodyLarge)
                        Spacer(Modifier.weight(1f))
                        Text(
                            "${sliderState.value.roundToInt()}ms",
                            style = typography.labelMedium,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                    Slider(
                        sliderState, modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                    )
                }
            }
            item {
                val sliderState = rememberSliderState(
                    value = 255f,
                    valueRange = 2f..255f,
                    onValueChangeFinished = {}
                )

                Column(
                    Modifier
                        .background(listItemColors.containerColor, bottomListItemShape)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    ) {
                        Text("Strength", style = typography.bodyLarge)
                        Spacer(Modifier.weight(1f))
                        Text(
                            "${((sliderState.value * 100) / 255f).roundToInt()}%",
                            style = typography.labelMedium,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                    Slider(
                        sliderState, modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                    )
                }
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
        contentPadding = PaddingValues(),
        onAction = {},
        onBack = {}
    )
}
