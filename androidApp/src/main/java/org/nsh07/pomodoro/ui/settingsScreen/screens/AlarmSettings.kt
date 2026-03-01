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

import android.content.Context.VIBRATOR_MANAGER_SERVICE
import android.content.Context.VIBRATOR_SERVICE
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.VibrationEffect.DEFAULT_AMPLITUDE
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.core.net.toUri
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.nsh07.pomodoro.data.FileLocator
import org.nsh07.pomodoro.ui.mergePaddingValues
import org.nsh07.pomodoro.ui.rememberRingtonePickerLauncherCallback
import org.nsh07.pomodoro.ui.settingsScreen.SettingsSwitchItem
import org.nsh07.pomodoro.ui.settingsScreen.components.PlusDivider
import org.nsh07.pomodoro.ui.settingsScreen.components.SliderListItem
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsAction
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsState
import org.nsh07.pomodoro.ui.theme.CustomColors.detailPaneTopBarColors
import org.nsh07.pomodoro.ui.theme.CustomColors.listItemColors
import org.nsh07.pomodoro.ui.theme.CustomColors.switchColors
import org.nsh07.pomodoro.ui.theme.CustomColors.topBarColors
import org.nsh07.pomodoro.ui.theme.LocalAppFonts
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.PANE_MAX_WIDTH
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.bottomListItemShape
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.cardShape
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.middleListItemShape
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.topListItemShape
import org.nsh07.pomodoro.ui.theme.TomatoTheme
import tomato.shared.generated.resources.Res
import tomato.shared.generated.resources.airwave
import tomato.shared.generated.resources.alarm
import tomato.shared.generated.resources.alarm_desc
import tomato.shared.generated.resources.alarm_on
import tomato.shared.generated.resources.alarm_sound
import tomato.shared.generated.resources.arrow_back
import tomato.shared.generated.resources.arrow_forward_big
import tomato.shared.generated.resources.back
import tomato.shared.generated.resources.bolt
import tomato.shared.generated.resources.check
import tomato.shared.generated.resources.clear
import tomato.shared.generated.resources.duration
import tomato.shared.generated.resources.gap
import tomato.shared.generated.resources.media_volume_for_alarm
import tomato.shared.generated.resources.media_volume_for_alarm_desc
import tomato.shared.generated.resources.menu
import tomato.shared.generated.resources.milliseconds_format
import tomato.shared.generated.resources.mobile_vibrate
import tomato.shared.generated.resources.music_note
import tomato.shared.generated.resources.play
import tomato.shared.generated.resources.restore_default
import tomato.shared.generated.resources.settings
import tomato.shared.generated.resources.sound
import tomato.shared.generated.resources.stop
import tomato.shared.generated.resources.system_default
import tomato.shared.generated.resources.vibrate
import tomato.shared.generated.resources.vibrate_desc
import tomato.shared.generated.resources.vibration_pattern
import tomato.shared.generated.resources.vibration_strength
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AlarmSettings(
    settingsState: SettingsState,
    isPlus: Boolean,
    contentPadding: PaddingValues,
    onAction: (SettingsAction) -> Unit,
    setShowPaywall: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current

    val widthExpanded = currentWindowAdaptiveInfo()
        .windowSizeClass
        .isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)

    var alarmName by remember { mutableStateOf("...") }
    var vibrationPlaying by remember { mutableStateOf(false) }
    val msFormat = stringResource(Res.string.milliseconds_format)

    LaunchedEffect(settingsState.alarmSoundUri) {
        withContext(Dispatchers.IO) {
            alarmName = try {
                RingtoneManager.getRingtone(context, settingsState.alarmSoundUri?.toUri())
                    ?.getTitle(context) ?: ""
            } catch (e: Exception) {
                Log.e("AlarmSettings", "Unable to get ringtone title: ${e.message}")
                e.printStackTrace()
                ""
            }
        }
    }

    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION") context.getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
    }

    val hasVibrator = vibrator.hasVibrator()
    val hasAmplitudeControl = vibrator.hasAmplitudeControl()

    DisposableEffect(Unit) {
        onDispose { vibrator.cancel() }
    }

    val ringtonePickerLauncherCallback = rememberRingtonePickerLauncherCallback(
        alarmSoundFileLocator = remember { FileLocator(settingsState.alarmSoundUri?.toUri()) },
        onResult = onAction
    )

    val switchItems = remember(
        settingsState.alarmEnabled,
        settingsState.vibrateEnabled,
        settingsState.mediaVolumeForAlarm
    ) {
        listOf(
            listOf(
                SettingsSwitchItem(
                    checked = settingsState.alarmEnabled,
                    icon = Res.drawable.alarm_on,
                    label = Res.string.sound,
                    description = Res.string.alarm_desc,
                    onClick = { onAction(SettingsAction.SaveAlarmEnabled(it)) }
                ),
                SettingsSwitchItem(
                    checked = settingsState.vibrateEnabled,
                    icon = Res.drawable.mobile_vibrate,
                    label = Res.string.vibrate,
                    description = Res.string.vibrate_desc,
                    onClick = { onAction(SettingsAction.SaveVibrateEnabled(it)) }
                )
            ),
            listOf(
                SettingsSwitchItem(
                    checked = settingsState.mediaVolumeForAlarm,
                    collapsible = true,
                    icon = Res.drawable.music_note,
                    label = Res.string.media_volume_for_alarm,
                    description = Res.string.media_volume_for_alarm_desc,
                    onClick = { onAction(SettingsAction.SaveMediaVolumeForAlarm(it)) }
                )
            )
        )
    }

    val barColors = if (widthExpanded) detailPaneTopBarColors
    else topBarColors

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(barColors.containerColor)
    ) {
        Scaffold(
            topBar = {
                LargeFlexibleTopAppBar(
                    title = {
                        Text(
                            stringResource(Res.string.alarm),
                            fontFamily = LocalAppFonts.current.topBarTitle
                        )
                    },
                    subtitle = {
                        Text(stringResource(Res.string.settings))
                    },
                    navigationIcon = {
                        if (!widthExpanded)
                            FilledTonalIconButton(
                                onClick = onBack,
                                shapes = IconButtonDefaults.shapes(),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = listItemColors.containerColor
                                )
                            ) {
                                Icon(
                                    painterResource(Res.drawable.arrow_back),
                                    stringResource(Res.string.back)
                                )
                            }
                    },
                    colors = barColors,
                    scrollBehavior = scrollBehavior
                )
            },
            containerColor = barColors.containerColor,
            modifier = modifier
                .widthIn(max = PANE_MAX_WIDTH)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) { innerPadding ->
            val insets = mergePaddingValues(innerPadding, contentPadding)
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                contentPadding = insets,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Spacer(Modifier.height(14.dp))
                }

                item {
                    ListItem(
                        leadingContent = {
                            Icon(painterResource(Res.drawable.alarm), null)
                        },
                        headlineContent = { Text(stringResource(Res.string.alarm_sound)) },
                        supportingContent = { Text(alarmName) },
                        trailingContent = {
                            Icon(
                                painterResource(Res.drawable.arrow_forward_big),
                                null
                            )
                        },
                        colors = listItemColors,
                        modifier = Modifier
                            .clip(topListItemShape)
                            .clickable(onClick = ringtonePickerLauncherCallback)
                    )
                }
                switchItems.fastForEachIndexed { baseIndex, items ->
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
                                                painter = painterResource(Res.drawable.check),
                                                contentDescription = null,
                                                modifier = Modifier.size(SwitchDefaults.IconSize),
                                            )
                                        } else {
                                            Icon(
                                                painter = painterResource(Res.drawable.clear),
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

                    if (baseIndex != switchItems.lastIndex)
                        item {
                            Spacer(Modifier.height(12.dp))
                        }
                }

                if (hasVibrator) {
                    if (!isPlus) item { PlusDivider(setShowPaywall) }
                    else item { Spacer(Modifier.height(12.dp)) }

                    item {
                        val interactionSources = remember { List(2) { MutableInteractionSource() } }

                        ListItem(
                            headlineContent = { Text(stringResource(Res.string.vibration_pattern)) },
                            trailingContent = {
                                ButtonGroup(
                                    overflowIndicator = {},
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    customItem(
                                        buttonGroupContent = {
                                            FilledIconToggleButton(
                                                checked = vibrationPlaying,
                                                onCheckedChange = {
                                                    vibrationPlaying = it
                                                    if (it && vibrator.hasVibrator()) {
                                                        val timings = longArrayOf(
                                                            0,
                                                            settingsState.vibrationOnDuration,
                                                            settingsState.vibrationOffDuration,
                                                            settingsState.vibrationOnDuration
                                                        )
                                                        val amplitudes = intArrayOf(
                                                            0,
                                                            settingsState.vibrationAmplitude,
                                                            0,
                                                            settingsState.vibrationAmplitude
                                                        )
                                                        val repeat = 2
                                                        val effect = VibrationEffect.createWaveform(
                                                            timings,
                                                            amplitudes,
                                                            repeat
                                                        )
                                                        vibrator.vibrate(effect)
                                                    } else {
                                                        vibrator.cancel()
                                                    }
                                                },
                                                interactionSource = interactionSources[0],
                                                modifier = Modifier
                                                    .size(52.dp, 40.dp)
                                                    .animateWidth(interactionSources[0])
                                            ) {
                                                if (vibrationPlaying)
                                                    Icon(painterResource(Res.drawable.stop), null)
                                                else
                                                    Icon(painterResource(Res.drawable.play), null)
                                            }
                                        },
                                        menuContent = {}
                                    )
                                    customItem(
                                        buttonGroupContent = {
                                            FilledTonalIconButton(
                                                onClick = {
                                                    onAction(
                                                        SettingsAction.SaveVibrationOnDuration(
                                                            1000L
                                                        )
                                                    )
                                                    onAction(
                                                        SettingsAction.SaveVibrationOffDuration(
                                                            1000L
                                                        )
                                                    )
                                                    onAction(
                                                        SettingsAction.SaveVibrationAmplitude(
                                                            DEFAULT_AMPLITUDE
                                                        )
                                                    )
                                                },
                                                enabled = isPlus,
                                                shapes = IconButtonDefaults.shapes(),
                                                interactionSource = interactionSources[1],
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .animateWidth(interactionSources[1])
                                            ) {
                                                Icon(
                                                    painterResource(Res.drawable.restore_default),
                                                    null
                                                )
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
                        SliderListItem(
                            value = settingsState.vibrationOnDuration.toFloat(),
                            valueRange = 10f..5000f,
                            enabled = isPlus,
                            label = stringResource(Res.string.duration),
                            trailingLabel = { String.format(msFormat, it.roundToInt()) },
                            trailingIcon = { Icon(painterResource(Res.drawable.airwave), null) },
                            shape = middleListItemShape
                        ) { onAction(SettingsAction.SaveVibrationOnDuration(it.roundToLong())) }
                    }
                    item {
                        SliderListItem(
                            value = settingsState.vibrationOffDuration.toFloat(),
                            valueRange = 10f..5000f,
                            enabled = isPlus,
                            label = stringResource(Res.string.gap),
                            trailingLabel = { String.format(msFormat, it.roundToInt()) },
                            trailingIcon = { Icon(painterResource(Res.drawable.menu), null) },
                            shape = if (hasAmplitudeControl) middleListItemShape else bottomListItemShape
                        ) { onAction(SettingsAction.SaveVibrationOffDuration(it.roundToLong())) }
                    }

                    if (hasAmplitudeControl) item {
                        val systemDefaultText = stringResource(Res.string.system_default)
                        SliderListItem(
                            value = if (settingsState.vibrationAmplitude == DEFAULT_AMPLITUDE) 127f
                            else settingsState.vibrationAmplitude.toFloat(),
                            valueRange = 2f..255f,
                            enabled = isPlus,
                            label = stringResource(Res.string.vibration_strength),
                            trailingLabel = {
                                if (settingsState.vibrationAmplitude == DEFAULT_AMPLITUDE)
                                    systemDefaultText
                                else "${((it * 100) / 255f).roundToInt()}%"
                            },
                            trailingIcon = { Icon(painterResource(Res.drawable.bolt), null) },
                            shape = bottomListItemShape
                        ) { onAction(SettingsAction.SaveVibrationAmplitude(it.roundToInt())) }
                    }
                }

                item { Spacer(Modifier.height(14.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Preview
@Composable
fun AlarmSettingsPreview() {
    val settingsState = SettingsState()
    TomatoTheme {
        AlarmSettings(
            settingsState = settingsState,
            contentPadding = PaddingValues(),
            isPlus = false,
            onAction = {},
            setShowPaywall = {},
            onBack = {}
        )
    }
}
