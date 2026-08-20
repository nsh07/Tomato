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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.nsh07.pomodoro.data.Topic
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsAction
import org.nsh07.pomodoro.ui.theme.SeededTheme
import org.nsh07.pomodoro.ui.theme.TomatoTheme

/**
 * A bottom sheet that lets the user edit the name, the color and the shape of [topic].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicShapeColorBottomSheet(
    topic: Topic,
    topics: List<Topic>,
    setShowSheet: (Boolean) -> Unit,
    onAction: (SettingsAction) -> Unit,
    modifier: Modifier = Modifier
) {
    SeededTheme(topic.color) {
        val colorScheme = MaterialTheme.colorScheme

        var name by remember { mutableStateOf(topic.name) }
        val nameTaken = remember(name, topics, topic.id) {
            val trimmedName = name.trim()
            topics.any { it.id != topic.id && it.name.equals(trimmedName, true) }
        }

        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onDismissRequest = { setShowSheet(false) },
            containerColor = colorScheme.surfaceContainer,
            modifier = modifier
        ) {
            TopicShapeColorPicker(
                name = name,
                onNameValueChange = { name = it },
                color = topic.color,
                shape = topic.shape,
                nameTaken = nameTaken,
                onNameChange = { onAction(SettingsAction.SetEditingTopicName(it)) },
                onColorChange = { onAction(SettingsAction.SetEditingTopicColor(it)) },
                onShapeChange = { onAction(SettingsAction.SetEditingTopicShape(it)) },
                containerColor = colorScheme.surfaceContainer,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .verticalScroll(rememberScrollState())
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun TopicShapeColorBottomSheetPreview() {
    var topic by remember { mutableStateOf(Topic.defaultTopic) }
    TomatoTheme(dynamicColor = false) {
        Surface(Modifier.fillMaxSize()) {
            TopicShapeColorBottomSheet(
                topic = topic,
                topics = listOf(topic),
                setShowSheet = {},
                onAction = { action ->
                    when (action) {
                        is SettingsAction.SetEditingTopicName -> topic =
                            topic.copy(name = action.name)

                        is SettingsAction.SetEditingTopicShape -> topic =
                            topic.copy(shape = action.shape)

                        is SettingsAction.SetEditingTopicColor -> topic =
                            topic.copy(color = action.color)

                        else -> Unit
                    }
                }
            )
        }
    }
}
