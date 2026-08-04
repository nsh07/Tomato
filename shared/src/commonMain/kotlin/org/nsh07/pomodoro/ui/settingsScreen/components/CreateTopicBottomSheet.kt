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

package org.nsh07.pomodoro.ui.settingsScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.nsh07.pomodoro.data.Topic
import org.nsh07.pomodoro.data.TopicShape
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsAction
import org.nsh07.pomodoro.ui.theme.LocalAppFonts
import org.nsh07.pomodoro.ui.theme.SeededTheme
import org.nsh07.pomodoro.ui.theme.TomatoTheme
import tomato.shared.generated.resources.Res
import tomato.shared.generated.resources.add_topic
import tomato.shared.generated.resources.cancel
import tomato.shared.generated.resources.create_new_topic

/**
 * A bottom sheet that lets the user create a new topic by picking its name, color and shape.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CreateTopicBottomSheet(
    topics: List<Topic>,
    setShowSheet: (Boolean) -> Unit,
    onAction: (SettingsAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val nameState = rememberTextFieldState()
    var color by remember { mutableStateOf(Color.White) }
    var shape by remember { mutableStateOf(TopicShape.COOKIE_12_SIDED) }

    val name = nameState.text.toString().trim()
    val nameTaken = remember(name, topics) {
        topics.any { it.name.equals(name, true) }
    }

    fun hideSheet(onHidden: () -> Unit = {}) {
        coroutineScope
            .launch { sheetState.hide() }
            .invokeOnCompletion {
                if (!sheetState.isVisible) {
                    setShowSheet(false)
                    onHidden()
                }
            }
    }

    SeededTheme(color) {
        val colorScheme = MaterialTheme.colorScheme

        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { setShowSheet(false) },
            containerColor = colorScheme.surfaceContainer,
            modifier = modifier
        ) {
            CreateTopicSheetContent(
                nameState = nameState,
                color = color,
                shape = shape,
                nameTaken = nameTaken,
                createEnabled = name.isNotEmpty() && !nameTaken,
                onColorChange = { color = it },
                onShapeChange = { shape = it },
                onCancel = { hideSheet() },
                onCreate = {
                    hideSheet {
                        onAction(
                            SettingsAction.CreateTopic(
                                Topic.defaultTopic.copy(
                                    id = 0,
                                    name = name,
                                    color = color,
                                    shape = shape
                                )
                            )
                        )
                    }
                },
                containerColor = colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CreateTopicSheetContent(
    nameState: TextFieldState,
    color: Color,
    shape: TopicShape,
    nameTaken: Boolean,
    createEnabled: Boolean,
    onColorChange: (Color) -> Unit,
    onShapeChange: (TopicShape) -> Unit,
    onCancel: () -> Unit,
    onCreate: () -> Unit,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Box(modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(bottom = 96.dp)
        ) {
            Text(
                stringResource(Res.string.create_new_topic),
                style = typography.titleLargeEmphasized,
                fontFamily = LocalAppFonts.current.topBarTitle,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
            )

            TopicShapeColorPicker(
                nameState = nameState,
                color = color,
                shape = shape,
                nameTaken = nameTaken,
                onNameChange = {},
                onColorChange = onColorChange,
                onShapeChange = onShapeChange,
                containerColor = containerColor,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            colorScheme.surfaceContainer.copy(0.9f),
                            colorScheme.surfaceContainer
                        ),
                        start = Offset.Zero,
                        end = Offset(x = Offset.Zero.x, y = Offset.Infinite.y)
                    )
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            TextButton(onClick = onCancel) {
                Text(stringResource(Res.string.cancel))
            }
            Button(
                onClick = onCreate,
                enabled = createEnabled
            ) {
                Text(stringResource(Res.string.add_topic))
            }
        }
    }
}

@Preview(widthDp = 412, heightDp = 600)
@Composable
private fun CreateTopicSheetContentPreview() {
    val nameState = rememberTextFieldState()
    var color by remember { mutableStateOf(Color.White) }
    var shape by remember { mutableStateOf(TopicShape.COOKIE_12_SIDED) }

    TomatoTheme(dynamicColor = false) {
        SeededTheme(color) {
            val colorScheme = MaterialTheme.colorScheme

            Surface(color = colorScheme.surfaceContainer, modifier = Modifier.fillMaxSize()) {
                CreateTopicSheetContent(
                    nameState = nameState,
                    color = color,
                    shape = shape,
                    nameTaken = false,
                    createEnabled = nameState.text.isNotBlank(),
                    onColorChange = { color = it },
                    onShapeChange = { shape = it },
                    onCancel = {},
                    onCreate = {},
                    containerColor = colorScheme.surfaceContainer,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
