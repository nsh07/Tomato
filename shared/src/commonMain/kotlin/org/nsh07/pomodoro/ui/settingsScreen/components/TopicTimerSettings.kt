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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
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
import org.nsh07.pomodoro.ui.theme.CustomColors.listItemColors
import org.nsh07.pomodoro.ui.theme.CustomColors.switchColors
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.bottomListItemShape
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.cardShape
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.middleListItemShape
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.segmentedListItemShapes
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.topListItemShape
import org.nsh07.pomodoro.ui.theme.TomatoTheme
import tomato.shared.generated.resources.Res
import tomato.shared.generated.resources.auto_start_next_timer
import tomato.shared.generated.resources.auto_start_next_timer_desc
import tomato.shared.generated.resources.autoplay
import tomato.shared.generated.resources.check
import tomato.shared.generated.resources.clear
import tomato.shared.generated.resources.clocks
import tomato.shared.generated.resources.delete
import tomato.shared.generated.resources.delete_topic
import tomato.shared.generated.resources.dnd
import tomato.shared.generated.resources.dnd_desc
import tomato.shared.generated.resources.edit_topic_desc
import tomato.shared.generated.resources.focus
import tomato.shared.generated.resources.info
import tomato.shared.generated.resources.long_break
import tomato.shared.generated.resources.session_length
import tomato.shared.generated.resources.session_length_desc
import tomato.shared.generated.resources.settings_infinite_focus_tip
import tomato.shared.generated.resources.short_break
import tomato.shared.generated.resources.style
import tomato.shared.generated.resources.timer_settings_reset_info

private enum class TopicTimerTip { INFINITE_FOCUS, RESET }

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalFoundationStyleApi::class
)
@Composable
fun TopicTimerSettings(
    topic: Topic,
    topics: List<Topic>,
    topicRunning: Boolean,
    focusTimeInputFieldState: TextFieldState,
    shortBreakTimeInputFieldState: TextFieldState,
    longBreakTimeInputFieldState: TextFieldState,
    sessionsSliderState: SliderState,
    onAction: (SettingsAction) -> Unit,
    modifier: Modifier = Modifier,
    inTimerScreen: Boolean = true
) {
    var showColorShapeSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val colorScheme = colorScheme

    Column(
        verticalArrangement = Arrangement.spacedBy(if (inTimerScreen) 2.dp else 16.dp),
        modifier = modifier
    ) {
        if (!inTimerScreen) {
            TopicShapeColorPickerButton(
                showColorShapeSheet = showColorShapeSheet,
                onShowColorShapeSheet = { showColorShapeSheet = it }
            )
        }

        if (inTimerScreen) {
            val tip = if (topicRunning) TopicTimerTip.RESET else TopicTimerTip.INFINITE_FOCUS

            AnimatedContent(tip) { targetTip ->
                when (targetTip) {
                    TopicTimerTip.INFINITE_FOCUS -> CompositionLocalProvider(
                        LocalContentColor provides colorScheme.onSurfaceVariant
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(painterResource(Res.drawable.info), null)
                            Text(stringResource(Res.string.settings_infinite_focus_tip))
                        }
                    }

                    TopicTimerTip.RESET -> CompositionLocalProvider(
                        LocalContentColor provides colorScheme.error
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(painterResource(Res.drawable.info), null)
                            Text(stringResource(Res.string.timer_settings_reset_info))
                        }
                    }
                }
            }
        } else {
            AnimatedVisibility(topicRunning) {
                CompositionLocalProvider(LocalContentColor provides colorScheme.error) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(painterResource(Res.drawable.info), null)
                        Text(stringResource(Res.string.timer_settings_reset_info))
                    }
                }
            }
        }

        TopicTimerProperties(
            topicId = topic.id,
            autostartNextSession = topic.autostartNextSession,
            dndEnabled = topic.dndEnabled,
            topicRunning = topicRunning,
            inTimerScreen = inTimerScreen,
            showDeleteDialog = showDeleteDialog,
            showColorShapeSheet = showColorShapeSheet,
            focusTimeInputFieldState = focusTimeInputFieldState,
            shortBreakTimeInputFieldState = shortBreakTimeInputFieldState,
            longBreakTimeInputFieldState = longBreakTimeInputFieldState,
            sessionsSliderState = sessionsSliderState,
            onAutostartNextSessionChange = {
                onAction(SettingsAction.SaveAutostartNextSession(it))
            },
            onDndEnabledChange = { onAction(SettingsAction.SaveDndEnabled(it)) },
            onShowColorShapeSheet = { showColorShapeSheet = it },
            setShowDeleteDialog = { showDeleteDialog = it }
        )
    }

    if (showColorShapeSheet) {
        TopicShapeColorBottomSheet(
            topic = topic,
            topics = topics,
            setShowSheet = { showColorShapeSheet = it },
            onAction = onAction
        )
    }

    if (showDeleteDialog) {
        val defaultTopicName = remember(topics) {
            topics.find { it.id == defaultTopic.id }?.name ?: defaultTopic.name
        }
        DeleteTopicDialog(
            defaultTopicName = defaultTopicName,
            onDismiss = { showDeleteDialog = false },
            onDeleteTopic = {
                showDeleteDialog = false
                onAction(SettingsAction.DeleteTopic(topic, deleteStats = false))
            },
            onDeleteTopicAndStats = {
                showDeleteDialog = false
                onAction(SettingsAction.DeleteTopic(topic, deleteStats = true))
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TopicTimerProperties(
    topicId: Long,
    autostartNextSession: Boolean,
    dndEnabled: Boolean,
    topicRunning: Boolean,
    focusTimeInputFieldState: TextFieldState,
    shortBreakTimeInputFieldState: TextFieldState,
    longBreakTimeInputFieldState: TextFieldState,
    sessionsSliderState: SliderState,
    inTimerScreen: Boolean,
    onAutostartNextSessionChange: (Boolean) -> Unit,
    onDndEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    showColorShapeSheet: Boolean = false,
    showDeleteDialog: Boolean = false,
    onShowColorShapeSheet: (Boolean) -> Unit = {},
    setShowDeleteDialog: (Boolean) -> Unit = {},
    showEditButtons: Boolean = true
) {
    val requestDndPermissionCallback = rememberRequestDndPermissionCallback()

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
                    enabled = !topicRunning,
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
                    enabled = !topicRunning,
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
                    enabled = !topicRunning,
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
                    enabled = !topicRunning,
                    modifier = Modifier
                        .padding(start = (16 * 2 + 24).dp, end = 16.dp, bottom = 12.dp)
                )
            }

            val switchItems = remember(
                autostartNextSession,
                dndEnabled,
                topicRunning
            ) {
                listOf(
                    SettingsSwitchItem(
                        checked = autostartNextSession,
                        icon = Res.drawable.autoplay,
                        label = Res.string.auto_start_next_timer,
                        description = Res.string.auto_start_next_timer_desc,
                        onClick = onAutostartNextSessionChange
                    ),
                    SettingsSwitchItem(
                        checked = dndEnabled,
                        enabled = !topicRunning,
                        icon = Res.drawable.dnd,
                        label = Res.string.dnd,
                        description = Res.string.dnd_desc,
                        onClick = {
                            requestDndPermissionCallback(it)
                            onDndEnabledChange(it)
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
                    shapes = segmentedListItemShapes(
                        index + 1,
                        switchItems.size + 1
                    ),
                    colors = listItemColors
                )
            }

            if (showEditButtons) {
                if (inTimerScreen) {
                    TopicShapeColorPickerButton(
                        showColorShapeSheet = showColorShapeSheet,
                        onShowColorShapeSheet = onShowColorShapeSheet
                    )
                }

                if (topicId != Topic.DEFAULT_TOPIC_ID) {
                    ToggleButton(
                        checked = showDeleteDialog,
                        onCheckedChange = setShowDeleteDialog,
                        shapes = ToggleButtonDefaults.shapes(),
                        colors = ToggleButtonDefaults.toggleButtonColors(
                            containerColor = colorScheme.errorContainer,
                            contentColor = colorScheme.onErrorContainer,
                            checkedContainerColor = colorScheme.error,
                            checkedContentColor = colorScheme.onError
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Icon(
                            painterResource(Res.drawable.delete),
                            null,
                            Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(Res.string.delete_topic),
                            style = typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun TopicShapeColorPickerButton(
    showColorShapeSheet: Boolean,
    onShowColorShapeSheet: (Boolean) -> Unit
) {
    ToggleButton(
        checked = showColorShapeSheet,
        onCheckedChange = onShowColorShapeSheet,
        shapes = ToggleButtonDefaults.shapes(),
        colors = ToggleButtonDefaults.toggleButtonColors(
            containerColor = colorScheme.tertiaryContainer,
            contentColor = colorScheme.onTertiaryContainer,
            checkedContainerColor = colorScheme.tertiary,
            checkedContentColor = colorScheme.onTertiary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Icon(
            painterResource(Res.drawable.style),
            null,
            Modifier.size(24.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(stringResource(Res.string.edit_topic_desc), style = typography.titleMedium)
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
                topics = sampleTopics,
                topicRunning = false,
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
                topics = sampleTopics,
                topicRunning = false,
                focusTimeInputFieldState = TextFieldState("25"),
                shortBreakTimeInputFieldState = TextFieldState("5"),
                longBreakTimeInputFieldState = TextFieldState("15"),
                sessionsSliderState = rememberSliderState(4f, valueRange = 1f..10f),
                onAction = {}
            )
        }
    }
}
