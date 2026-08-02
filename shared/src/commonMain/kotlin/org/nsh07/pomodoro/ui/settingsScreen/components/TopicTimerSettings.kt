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

package org.nsh07.pomodoro.ui.settingsScreen.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.styleable
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.nsh07.pomodoro.data.Topic
import org.nsh07.pomodoro.data.Topic.Companion.defaultTopic
import org.nsh07.pomodoro.ui.rememberRequestDndPermissionCallback
import org.nsh07.pomodoro.ui.settingsScreen.SettingsSwitchItem
import org.nsh07.pomodoro.ui.settingsScreen.screens.sampleTopics
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsAction
import org.nsh07.pomodoro.ui.theme.CustomColors.black
import org.nsh07.pomodoro.ui.theme.CustomColors.listItemColors
import org.nsh07.pomodoro.ui.theme.CustomColors.switchColors
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.bottomListItemShape
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.cardShape
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.middleListItemShape
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.segmentedListItemShapes
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.topListItemShape
import org.nsh07.pomodoro.ui.theme.TomatoTheme
import org.nsh07.pomodoro.ui.theme.harmonizeIf
import tomato.shared.generated.resources.Res
import tomato.shared.generated.resources.auto_start_next_timer
import tomato.shared.generated.resources.auto_start_next_timer_desc
import tomato.shared.generated.resources.autoplay
import tomato.shared.generated.resources.check
import tomato.shared.generated.resources.clear
import tomato.shared.generated.resources.clocks
import tomato.shared.generated.resources.dnd
import tomato.shared.generated.resources.dnd_desc
import tomato.shared.generated.resources.focus
import tomato.shared.generated.resources.long_break
import tomato.shared.generated.resources.session_length
import tomato.shared.generated.resources.session_length_desc
import tomato.shared.generated.resources.short_break

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalFoundationStyleApi::class
)
@Composable
fun TopicTimerSettings(
    topic: Topic,
    serviceRunning: Boolean,
    focusTimeInputFieldState: TextFieldState,
    shortBreakTimeInputFieldState: TextFieldState,
    longBreakTimeInputFieldState: TextFieldState,
    sessionsSliderState: SliderState,
    onAction: (SettingsAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val requestDndPermissionCallback = rememberRequestDndPermissionCallback()
    val isDefaultTopic = topic.id == defaultTopic.id

    var showColorShapeSheet by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
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
                    stringResource(Res.string.focus),
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
                    stringResource(Res.string.short_break),
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
                    stringResource(Res.string.long_break),
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

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Column(Modifier.background(listItemColors.containerColor, topListItemShape)) {
                ListItem(
                    leadingContent = {
                        Icon(painterResource(Res.drawable.clocks), null)
                    },
                    headlineContent = {
                        Text(stringResource(Res.string.session_length))
                    },
                    supportingContent = {
                        Text(
                            stringResource(
                                Res.string.session_length_desc,
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

            val switchItems = remember(
                topic.autostartNextSession,
                topic.dndEnabled
            ) {
                listOf(
                    SettingsSwitchItem(
                        checked = topic.autostartNextSession,
                        icon = Res.drawable.autoplay,
                        label = Res.string.auto_start_next_timer,
                        description = Res.string.auto_start_next_timer_desc,
                        onClick = { onAction(SettingsAction.SaveAutostartNextSession(it)) }
                    ),
                    SettingsSwitchItem(
                        checked = topic.dndEnabled,
                        enabled = !serviceRunning,
                        icon = Res.drawable.dnd,
                        label = Res.string.dnd,
                        description = Res.string.dnd_desc,
                        onClick = {
                            requestDndPermissionCallback(it)
                            onAction(SettingsAction.SaveDndEnabled(it))
                        }
                    )
                )
            }

            switchItems.forEachIndexed { index, item ->
                SegmentedListItem(
                    onClick = { item.onClick(!item.checked) },
                    leadingContent = {
                        Icon(
                            painterResource(item.icon),
                            contentDescription = null
                        )
                    },
                    content = { Text(stringResource(item.label)) },
                    supportingContent = { Text(stringResource(item.description)) },
                    trailingContent = {
                        Switch(
                            checked = item.checked,
                            enabled = item.enabled,
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
                    shapes = segmentedListItemShapes(index + 1, switchItems.size + 1),
                    colors = listItemColors
                )
            }
        }

        AnimatedVisibility(!isDefaultTopic) {
            SegmentedListItem(
                checked = showColorShapeSheet,
                onCheckedChange = { showColorShapeSheet = it },
                leadingContent = {
                    val fillColor = topic.color.harmonizeIf(colorScheme.primary, isDefaultTopic)
                    val fillShape = topic.shape.toShape()
                    Box(
                        Modifier
                            .size(22.dp)
                            .styleable {
                                background(fillColor)
                                shape(fillShape)
                            }
                    )
                },
                shapes = segmentedListItemShapes(0, 1),
                colors = ListItemDefaults.segmentedColors(
                    containerColor = topic.color.harmonizeIf(
                        if (!black) colorScheme.surfaceBright else colorScheme.surfaceContainerHigh,
                        isDefaultTopic
                    ),
                    selectedContainerColor = topic.color.harmonizeIf(
                        listItemColors.selectedContainerColor,
                        isDefaultTopic
                    )
                )
            ) {
                Text("Topic shape & color")
            }
        }
    }

    if (showColorShapeSheet) {
        val colorSchemes = listOf(
            Color(0xfffeb4a7), Color(0xffffb3c0), Color(0xfffcaaff), Color(0xffb9c3ff),
            Color(0xff62d3ff), Color(0xff44d9f1), Color(0xff52dbc9), Color(0xff78dd77),
            Color(0xff9fd75c), Color(0xffc1d02d), Color(0xfffabd00), Color(0xffffb86e)
        )

        ModalBottomSheet(
            onDismissRequest = { showColorShapeSheet = false }
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Color",
                    style = typography.labelMedium,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                ColorPickerRow(
                    topic.color,
                    colorSchemes,
                    isPlus = true,
                    onColorChange = { onAction(SettingsAction.SetEditingTopicColor(it)) },
                    backgroundColor = colorScheme.surfaceContainerLow,
                    horizontalPadding = 16.dp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun TopicTimerSettingsPreview() {
    TomatoTheme(dynamicColor = false) {
        Box(Modifier.background(colorScheme.surfaceContainer)) {
            TopicTimerSettings(
                topic = sampleTopics.random(),
                serviceRunning = false,
                focusTimeInputFieldState = TextFieldState("25"),
                shortBreakTimeInputFieldState = TextFieldState("5"),
                longBreakTimeInputFieldState = TextFieldState("15"),
                sessionsSliderState = rememberSliderState(4f, valueRange = 1f..10f),
                onAction = {}
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun TopicTimerSettingsDarkPreview() {
    TomatoTheme(darkTheme = true, dynamicColor = false) {
        Box(Modifier.background(colorScheme.surfaceContainer)) {
            TopicTimerSettings(
                topic = sampleTopics.random(),
                serviceRunning = false,
                focusTimeInputFieldState = TextFieldState("25"),
                shortBreakTimeInputFieldState = TextFieldState("5"),
                longBreakTimeInputFieldState = TextFieldState("15"),
                sessionsSliderState = rememberSliderState(4f, valueRange = 1f..10f),
                onAction = {}
            )
        }
    }
}
