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
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.ui.mergePaddingValues
import org.nsh07.pomodoro.ui.settingsScreen.SettingsSwitchItem
import org.nsh07.pomodoro.ui.settingsScreen.components.MinuteInputField
import org.nsh07.pomodoro.ui.settingsScreen.components.MinutesInputTransformation3Digits
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
import org.nsh07.pomodoro.utils.millisecondsToHoursMinutes

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TimerSettings(
    isPlus: Boolean,
    serviceRunning: Boolean,
    settingsState: SettingsState,
    contentPadding: PaddingValues,
    focusTimeInputFieldState: TextFieldState,
    shortBreakTimeInputFieldState: TextFieldState,
    longBreakTimeInputFieldState: TextFieldState,
    sessionsSliderState: SliderState,
    onAction: (SettingsAction) -> Unit,
    setShowPaywall: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val context = LocalContext.current
    val inspectionMode = LocalInspectionMode.current
    val notificationManagerService = remember {
        if (!inspectionMode)
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        else
            null
    }

    val widthExpanded = currentWindowAdaptiveInfo()
        .windowSizeClass
        .isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)

    val switchItems = remember(
        settingsState.dndEnabled,
        settingsState.aodEnabled,
        settingsState.autostartNextSession,
        settingsState.secureAod,
        isPlus,
        serviceRunning
    ) {
        listOf(
            listOf(
                SettingsSwitchItem(
                    checked = settingsState.autostartNextSession,
                    icon = R.drawable.autoplay,
                    label = R.string.auto_start_next_timer,
                    description = R.string.auto_start_next_timer_desc,
                    onClick = { onAction(SettingsAction.SaveAutostartNextSession(it)) }
                ),
                SettingsSwitchItem(
                    checked = settingsState.dndEnabled,
                    enabled = !serviceRunning,
                    icon = R.drawable.dnd,
                    label = R.string.dnd,
                    description = R.string.dnd_desc,
                    onClick = {
                        if (it && notificationManagerService?.isNotificationPolicyAccessGranted() == false) {
                            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)

                            @SuppressLint("LocalContextGetResourceValueCall")
                            Toast
                                .makeText(
                                    context,
                                    context.getString(
                                        R.string.dnd_permission_message,
                                        context.getString(R.string.app_name)
                                    ),
                                    Toast.LENGTH_LONG
                                )
                                .show()

                            context.startActivity(intent)
                        } else if (!it && notificationManagerService?.isNotificationPolicyAccessGranted() == true) {
                            notificationManagerService.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                        }
                        onAction(SettingsAction.SaveDndEnabled(it))
                    }
                )
            ),
            listOf(
                SettingsSwitchItem(
                    checked = settingsState.aodEnabled,
                    enabled = isPlus,
                    icon = R.drawable.aod,
                    label = R.string.always_on_display,
                    description = R.string.always_on_display_desc,
                    onClick = { onAction(SettingsAction.SaveAodEnabled(it)) }
                ),
                SettingsSwitchItem(
                    checked = settingsState.secureAod && isPlus,
                    enabled = isPlus && settingsState.aodEnabled,
                    icon = R.drawable.mobile_lock_portrait,
                    label = R.string.secure_aod,
                    description = R.string.secure_aod_desc,
                    onClick = { onAction(SettingsAction.SaveSecureAod(it)) }
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
                            stringResource(R.string.timer),
                            fontFamily = LocalAppFonts.current.topBarTitle
                        )
                    },
                    subtitle = {
                        Text(stringResource(R.string.settings))
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
                                    painterResource(R.drawable.arrow_back),
                                    stringResource(R.string.back)

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
                    CompositionLocalProvider(LocalContentColor provides colorScheme.error) {
                        AnimatedVisibility(serviceRunning) {
                            Column {
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(painterResource(R.drawable.info), null)
                                    Text(stringResource(R.string.timer_settings_reset_info))
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                }
                item {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                stringResource(R.string.focus),
                                style = typography.titleSmallEmphasized
                            )
                            MinuteInputField(
                                state = focusTimeInputFieldState,
                                enabled = !serviceRunning,
                                shape = RoundedCornerShape(
                                    topStart = topListItemShape.topStart,
                                    bottomStart = topListItemShape.topStart,
                                    topEnd = topListItemShape.bottomStart,
                                    bottomEnd = topListItemShape.bottomStart
                                ),
                                inputTransformation = MinutesInputTransformation3Digits,
                                imeAction = ImeAction.Next
                            )
                        }
                        Spacer(Modifier.width(2.dp))
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                stringResource(R.string.short_break),
                                style = typography.titleSmallEmphasized
                            )
                            MinuteInputField(
                                state = shortBreakTimeInputFieldState,
                                enabled = !serviceRunning,
                                shape = RoundedCornerShape(middleListItemShape.topStart),
                                imeAction = ImeAction.Next
                            )
                        }
                        Spacer(Modifier.width(2.dp))
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                stringResource(R.string.long_break),
                                style = typography.titleSmallEmphasized
                            )
                            MinuteInputField(
                                state = longBreakTimeInputFieldState,
                                enabled = !serviceRunning,
                                shape = RoundedCornerShape(
                                    topStart = bottomListItemShape.topStart,
                                    bottomStart = bottomListItemShape.topStart,
                                    topEnd = bottomListItemShape.bottomStart,
                                    bottomEnd = bottomListItemShape.bottomStart
                                ),
                                imeAction = ImeAction.Done
                            )
                        }
                    }
                }
                item {
                    Spacer(Modifier.height(12.dp))
                }
                item {
                    Column(Modifier.background(listItemColors.containerColor, topListItemShape)) {
                        ListItem(
                            leadingContent = {
                                Icon(painterResource(R.drawable.clocks), null)
                            },
                            headlineContent = {
                                Text(stringResource(R.string.session_length))
                            },
                            supportingContent = {
                                Text(
                                    stringResource(
                                        R.string.session_length_desc,
                                        sessionsSliderState.value.toInt()
                                    )
                                )
                            },
                            colors = listItemColors,
                            modifier = Modifier.clip(cardShape)
                        )
                        Slider(
                            state = sessionsSliderState,
                            enabled = !serviceRunning,
                            modifier = Modifier
                                .padding(start = (16 * 2 + 24).dp, end = 16.dp, bottom = 12.dp)
                        )
                    }
                }
                item {
                    val hmf = stringResource(R.string.hours_and_minutes_format)
                    SliderListItem(
                        value = settingsState.focusGoal.toFloat(),
                        valueRange = 0f..16 * 60 * 60 * 1000f,
                        enabled = true,
                        label = stringResource(R.string.daily_focus_goal),
                        trailingLabel = { millisecondsToHoursMinutes(it.toLong(), hmf) },
                        icon = { Icon(painterResource(R.drawable.flag), null) },
                        shape = bottomListItemShape
                    ) {
                        with(it.toLong()) {
                            onAction(SettingsAction.SaveFocusGoal(this - (this % (30 * 60 * 1000))))
                        }
                    }
                }
                item { Spacer(Modifier.height(12.dp)) }

                itemsIndexed(switchItems[0]) { index, item ->
                    ListItem(
                        leadingContent = {
                            Icon(
                                painterResource(item.icon),
                                contentDescription = null,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        },
                        headlineContent = { Text(stringResource(item.label)) },
                        supportingContent = { Text(stringResource(item.description)) },
                        trailingContent = {
                            Switch(
                                checked = item.checked,
                                enabled = item.enabled,
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
                        modifier = Modifier.clip(
                            when (index) {
                                0 -> topListItemShape
                                switchItems[0].size - 1 -> bottomListItemShape
                                else -> middleListItemShape
                            }
                        )
                    )
                }

                if (isPlus) {
                    item { Spacer(Modifier.height(12.dp)) }
                    itemsIndexed(switchItems[1]) { index, item ->
                        ListItem(
                            leadingContent = {
                                Icon(
                                    painterResource(item.icon),
                                    contentDescription = null,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            },
                            headlineContent = { Text(stringResource(item.label)) },
                            supportingContent = { Text(stringResource(item.description)) },
                            trailingContent = {
                                Switch(
                                    checked = item.checked,
                                    onCheckedChange = { item.onClick(it) },
                                    enabled = item.enabled,
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
                            modifier = Modifier.clip(
                                when (index) {
                                    0 -> topListItemShape
                                    switchItems[1].size - 1 -> bottomListItemShape
                                    else -> middleListItemShape
                                }
                            )
                        )
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                    item { Spacer(Modifier.height(12.dp)) }
                    item {
                        ListItem(
                            leadingContent = {
                                Icon(painterResource(R.drawable.view_day), null)
                            },
                            headlineContent = { Text(stringResource(R.string.session_only_progress)) },
                            supportingContent = {
                                var expanded by remember { mutableStateOf(false) }
                                Text(
                                    stringResource(R.string.session_only_progress_desc),
                                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .clickable { expanded = !expanded }
                                        .animateContentSize(motionScheme.defaultSpatialSpec())
                                )
                            },
                            trailingContent = {
                                Switch(
                                    checked = settingsState.singleProgressBar,
                                    enabled = !serviceRunning,
                                    onCheckedChange = {
                                        onAction(
                                            SettingsAction.SaveSingleProgressBar(
                                                it
                                            )
                                        )
                                    },
                                    thumbContent = {
                                        if (settingsState.singleProgressBar) {
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
                            modifier = Modifier.clip(cardShape)
                        )
                    }
                }

                if (!isPlus) {
                    item {
                        PlusDivider(setShowPaywall)
                    }
                    itemsIndexed(switchItems[1]) { index, item ->
                        ListItem(
                            leadingContent = {
                                Icon(
                                    painterResource(item.icon),
                                    contentDescription = null,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            },
                            headlineContent = { Text(stringResource(item.label)) },
                            supportingContent = { Text(stringResource(item.description)) },
                            trailingContent = {
                                Switch(
                                    checked = item.checked,
                                    onCheckedChange = { item.onClick(it) },
                                    enabled = item.enabled,
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
                            modifier = Modifier.clip(
                                when (index) {
                                    0 -> topListItemShape
                                    switchItems[1].size - 1 -> bottomListItemShape
                                    else -> middleListItemShape
                                }
                            )
                        )
                    }
                }

                item {
                    var expanded by remember { mutableStateOf(false) }
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier
                            .padding(vertical = 6.dp)
                            .fillMaxWidth()
                    ) {
                        FilledTonalIconToggleButton(
                            checked = expanded,
                            onCheckedChange = { expanded = it },
                            shapes = IconButtonDefaults.toggleableShapes(),
                            modifier = Modifier.width(52.dp)
                        ) {
                            Icon(
                                painterResource(R.drawable.info),
                                null
                            )
                        }
                        AnimatedVisibility(expanded) {
                            Text(
                                stringResource(R.string.pomodoro_info),
                                style = typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }

                item { Spacer(Modifier.height(12.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun TimerSettingsPreview() {
    val focusTimeInputFieldState = rememberTextFieldState("25")
    val shortBreakTimeInputFieldState = rememberTextFieldState("5")
    val longBreakTimeInputFieldState = rememberTextFieldState("15")
    val sessionsSliderState = rememberSliderState(
        value = 4f,
        valueRange = 1f..10f,
        steps = 8
    )
    TimerSettings(
        isPlus = false,
        serviceRunning = false,
        settingsState = remember { SettingsState() },
        contentPadding = PaddingValues(),
        focusTimeInputFieldState = focusTimeInputFieldState,
        shortBreakTimeInputFieldState = shortBreakTimeInputFieldState,
        longBreakTimeInputFieldState = longBreakTimeInputFieldState,
        sessionsSliderState = sessionsSliderState,
        onAction = {},
        setShowPaywall = {},
        onBack = {}
    )
}