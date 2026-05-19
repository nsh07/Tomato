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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SliderState
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import com.materialkolor.ktx.harmonize
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.nsh07.pomodoro.data.Topic
import org.nsh07.pomodoro.data.Topic.Companion.defaultTopic
import org.nsh07.pomodoro.ui.mergePaddingValues
import org.nsh07.pomodoro.ui.settingsScreen.SettingsSwitchItem
import org.nsh07.pomodoro.ui.settingsScreen.components.PlusDivider
import org.nsh07.pomodoro.ui.settingsScreen.components.SliderListItem
import org.nsh07.pomodoro.ui.settingsScreen.components.TopicTimerSettings
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
import org.nsh07.pomodoro.ui.topBarWindowInsets
import org.nsh07.pomodoro.utils.androidSdkVersionAtLeast
import org.nsh07.pomodoro.utils.millisecondsToHoursMinutes
import tomato.shared.generated.resources.Res
import tomato.shared.generated.resources.always_on_display
import tomato.shared.generated.resources.always_on_display_desc
import tomato.shared.generated.resources.aod
import tomato.shared.generated.resources.arrow_back
import tomato.shared.generated.resources.arrow_down
import tomato.shared.generated.resources.back
import tomato.shared.generated.resources.check
import tomato.shared.generated.resources.clear
import tomato.shared.generated.resources.daily_focus_goal
import tomato.shared.generated.resources.flag
import tomato.shared.generated.resources.hours_and_minutes_format
import tomato.shared.generated.resources.info
import tomato.shared.generated.resources.mobile_lock_portrait
import tomato.shared.generated.resources.pomodoro_info
import tomato.shared.generated.resources.secure_aod
import tomato.shared.generated.resources.secure_aod_desc
import tomato.shared.generated.resources.session_only_progress
import tomato.shared.generated.resources.session_only_progress_desc
import tomato.shared.generated.resources.settings
import tomato.shared.generated.resources.settings_infinite_focus_tip
import tomato.shared.generated.resources.timer
import tomato.shared.generated.resources.timer_settings_reset_info
import tomato.shared.generated.resources.view_day

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
    topics: List<Topic>,
    editingTopic: Topic,
    onAction: (SettingsAction) -> Unit,
    setShowPaywall: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val colorScheme = colorScheme

    val widthExpanded = currentWindowAdaptiveInfo()
        .windowSizeClass
        .isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)

    val switchItems = remember(
        settingsState.aodEnabled,
        settingsState.secureAod,
        isPlus,
        serviceRunning
    ) {
        listOf(
            SettingsSwitchItem(
                checked = settingsState.aodEnabled,
                enabled = isPlus,
                icon = Res.drawable.aod,
                label = Res.string.always_on_display,
                description = Res.string.always_on_display_desc,
                onClick = { onAction(SettingsAction.SaveAodEnabled(it)) }
            ),
            SettingsSwitchItem(
                checked = settingsState.secureAod && isPlus,
                enabled = isPlus && settingsState.aodEnabled,
                icon = Res.drawable.mobile_lock_portrait,
                label = Res.string.secure_aod,
                description = Res.string.secure_aod_desc,
                onClick = { onAction(SettingsAction.SaveSecureAod(it)) }
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
                    windowInsets = topBarWindowInsets(),
                    title = {
                        Text(
                            stringResource(Res.string.timer),
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
                    actions = {
                        var expanded by remember { mutableStateOf(false) }
                        val buttonColor by animateColorAsState(
                            editingTopic.color.let {
                                if (it != Color.White) {
                                    it.harmonize(colorScheme.primary, true)
                                } else colorScheme.primary
                            }
                        )
                        SplitButtonLayout(
                            leadingButton = {
                                SplitButtonDefaults.LeadingButton(
                                    onClick = {},
                                    content = {
                                        AnimatedContent(editingTopic.name) { Text(it) }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                                )
                            },
                            trailingButton = {
                                SplitButtonDefaults.TrailingButton(
                                    checked = expanded,
                                    onCheckedChange = { expanded = it },
                                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                                ) {
                                    val rotation: Float by animateFloatAsState(
                                        targetValue = if (expanded) 180f else 0f,
                                        label = "Trailing Icon Rotation"
                                    )
                                    Icon(
                                        painterResource(Res.drawable.arrow_down),
                                        modifier =
                                            Modifier.size(SplitButtonDefaults.TrailingIconSize)
                                                .graphicsLayer {
                                                    this.rotationZ = rotation
                                                },
                                        contentDescription = null
                                    )
                                }
                            }
                        )
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
                    Column {
                        Spacer(Modifier.height(8.dp))
                        AnimatedContent(serviceRunning) {
                            if (it) {
                                CompositionLocalProvider(LocalContentColor provides colorScheme.error) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(painterResource(Res.drawable.info), null)
                                        Text(stringResource(Res.string.timer_settings_reset_info))
                                    }
                                }
                            } else {
                                CompositionLocalProvider(LocalContentColor provides colorScheme.onSurfaceVariant) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(painterResource(Res.drawable.info), null)
                                        Text(stringResource(Res.string.settings_infinite_focus_tip))
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                }
                item {
                    TopicTimerSettings(
                        topic = editingTopic,
                        serviceRunning = serviceRunning,
                        focusTimeInputFieldState = focusTimeInputFieldState,
                        shortBreakTimeInputFieldState = shortBreakTimeInputFieldState,
                        longBreakTimeInputFieldState = longBreakTimeInputFieldState,
                        sessionsSliderState = sessionsSliderState,
                        onAction = onAction
                    )
                    Spacer(Modifier.height(14.dp))
                }

                item {
                    val hmf = stringResource(Res.string.hours_and_minutes_format)
                    SliderListItem(
                        value = settingsState.focusGoal.toFloat(),
                        valueRange = 0f..16 * 60 * 60 * 1000f,
                        enabled = true,
                        label = stringResource(Res.string.daily_focus_goal),
                        trailingLabel = { millisecondsToHoursMinutes(it.toLong(), hmf) },
                        icon = { Icon(painterResource(Res.drawable.flag), null) },
                        shape = shapes.large
                    ) {
                        with(it.toLong()) {
                            onAction(SettingsAction.SaveFocusGoal(this - (this % (30 * 60 * 1000))))
                        }
                    }
                }
                item { Spacer(Modifier.height(12.dp)) }

                if (isPlus) {
                    item { Spacer(Modifier.height(12.dp)) }
                    itemsIndexed(switchItems) { index, item ->
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
                            modifier = Modifier.clip(
                                when (index) {
                                    0 -> topListItemShape
                                    switchItems.size - 1 -> bottomListItemShape
                                    else -> middleListItemShape
                                }
                            )
                        )
                    }
                }

                if (androidSdkVersionAtLeast(36)) {
                    item { Spacer(Modifier.height(12.dp)) }
                    item {
                        ListItem(
                            leadingContent = {
                                Icon(painterResource(Res.drawable.view_day), null)
                            },
                            headlineContent = { Text(stringResource(Res.string.session_only_progress)) },
                            supportingContent = {
                                var expanded by remember { mutableStateOf(false) }
                                Text(
                                    stringResource(Res.string.session_only_progress_desc),
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
                            modifier = Modifier.clip(cardShape)
                        )
                    }
                }

                if (!isPlus) {
                    item {
                        PlusDivider(setShowPaywall)
                    }
                    itemsIndexed(switchItems) { index, item ->
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
                            modifier = Modifier.clip(
                                when (index) {
                                    0 -> topListItemShape
                                    switchItems.size - 1 -> bottomListItemShape
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
                                painterResource(Res.drawable.info),
                                null
                            )
                        }
                        AnimatedVisibility(expanded) {
                            Text(
                                stringResource(Res.string.pomodoro_info),
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
    TomatoTheme(dynamicColor = false) {
        TimerSettings(
            isPlus = false,
            serviceRunning = false,
            settingsState = remember { SettingsState() },
            contentPadding = PaddingValues(),
            focusTimeInputFieldState = focusTimeInputFieldState,
            shortBreakTimeInputFieldState = shortBreakTimeInputFieldState,
            longBreakTimeInputFieldState = longBreakTimeInputFieldState,
            sessionsSliderState = sessionsSliderState,
            topics = listOf(
                defaultTopic,
                defaultTopic.copy(id = "physics", name = "Physics"),
                defaultTopic.copy(id = "math", name = "Math"),
                defaultTopic.copy(id = "chemistry", name = "Chemistry")
            ),
            editingTopic = defaultTopic.copy(id = "math", name = "Math"),
            onAction = {},
            setShowPaywall = {},
            onBack = {}
        )
    }
}