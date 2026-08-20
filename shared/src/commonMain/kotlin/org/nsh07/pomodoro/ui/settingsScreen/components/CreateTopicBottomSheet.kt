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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SliderState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.nsh07.pomodoro.data.Topic
import org.nsh07.pomodoro.data.Topic.Companion.defaultTopic
import org.nsh07.pomodoro.data.TopicShape
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsAction
import org.nsh07.pomodoro.ui.theme.LocalAppFonts
import org.nsh07.pomodoro.ui.theme.SeededTheme
import org.nsh07.pomodoro.ui.theme.TomatoTheme
import tomato.shared.generated.resources.Res
import tomato.shared.generated.resources.add_topic
import tomato.shared.generated.resources.back
import tomato.shared.generated.resources.cancel
import tomato.shared.generated.resources.check
import tomato.shared.generated.resources.create_new_topic
import tomato.shared.generated.resources.keyboard_arrow_right
import tomato.shared.generated.resources.next

private enum class CreateTopicStep { Appearance, Timer }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CreateTopicBottomSheet(
    topics: List<Topic>,
    setShowSheet: (Boolean) -> Unit,
    onAction: (SettingsAction) -> Unit,
    modifier: Modifier = Modifier,
    setAsCurrent: Boolean = false
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var step by remember { mutableStateOf(CreateTopicStep.Appearance) }

    var name by remember { mutableStateOf("") }
    var color by remember { mutableStateOf(Color.White) }
    var shape by remember { mutableStateOf(defaultTopic.shape) }

    val focusTimeInputFieldState = rememberTextFieldState(defaultTopic.focusTime.toMinutes())
    val shortBreakTimeInputFieldState =
        rememberTextFieldState(defaultTopic.shortBreakTime.toMinutes())
    val longBreakTimeInputFieldState =
        rememberTextFieldState(defaultTopic.longBreakTime.toMinutes())
    val sessionsSliderState = rememberSliderState(
        value = defaultTopic.sessionLength.toFloat(),
        steps = 8,
        valueRange = 1f..10f
    )
    var autostartNextSession by remember { mutableStateOf(defaultTopic.autostartNextSession) }
    var dndEnabled by remember { mutableStateOf(defaultTopic.dndEnabled) }

    val trimmedName = name.trim()
    val nameTaken = remember(trimmedName, topics) {
        topics.any { it.name.equals(trimmedName, true) }
    }
    val nameValid = trimmedName.isNotEmpty() && !nameTaken
    val timesValid = focusTimeInputFieldState.text.isValidMinutesInput() &&
            shortBreakTimeInputFieldState.text.isValidMinutesInput() &&
            longBreakTimeInputFieldState.text.isValidMinutesInput()

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
        val colorScheme = colorScheme

        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { setShowSheet(false) },
            containerColor = colorScheme.surfaceContainer,
            modifier = modifier
        ) {
            CreateTopicSheetContent(
                step = step,
                name = name,
                onNameValueChange = { name = it },
                color = color,
                shape = shape,
                nameTaken = nameTaken,
                focusTimeInputFieldState = focusTimeInputFieldState,
                shortBreakTimeInputFieldState = shortBreakTimeInputFieldState,
                longBreakTimeInputFieldState = longBreakTimeInputFieldState,
                sessionsSliderState = sessionsSliderState,
                autostartNextSession = autostartNextSession,
                dndEnabled = dndEnabled,
                nextEnabled = nameValid,
                createEnabled = nameValid && timesValid,
                onColorChange = { color = it },
                onShapeChange = { shape = it },
                onAutostartNextSessionChange = { autostartNextSession = it },
                onDndEnabledChange = { dndEnabled = it },
                onCancel = { hideSheet() },
                onNext = { step = CreateTopicStep.Timer },
                onBack = { step = CreateTopicStep.Appearance },
                onCreate = {
                    hideSheet {
                        onAction(
                            SettingsAction.CreateTopic(
                                topic = defaultTopic.copy(
                                    id = 0,
                                    name = trimmedName,
                                    color = color,
                                    shape = shape,
                                    focusTime = focusTimeInputFieldState.toMillis(
                                        defaultTopic.focusTime
                                    ),
                                    shortBreakTime = shortBreakTimeInputFieldState.toMillis(
                                        defaultTopic.shortBreakTime
                                    ),
                                    longBreakTime = longBreakTimeInputFieldState.toMillis(
                                        defaultTopic.longBreakTime
                                    ),
                                    sessionLength = sessionsSliderState.value.toInt(),
                                    autostartNextSession = autostartNextSession,
                                    dndEnabled = dndEnabled
                                ),
                                setAsCurrent = setAsCurrent
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CreateTopicSheetContent(
    step: CreateTopicStep,
    name: String,
    onNameValueChange: (String) -> Unit,
    color: Color,
    shape: TopicShape,
    nameTaken: Boolean,
    focusTimeInputFieldState: TextFieldState,
    shortBreakTimeInputFieldState: TextFieldState,
    longBreakTimeInputFieldState: TextFieldState,
    sessionsSliderState: SliderState,
    autostartNextSession: Boolean,
    dndEnabled: Boolean,
    nextEnabled: Boolean,
    createEnabled: Boolean,
    onColorChange: (Color) -> Unit,
    onShapeChange: (TopicShape) -> Unit,
    onAutostartNextSessionChange: (Boolean) -> Unit,
    onDndEnabledChange: (Boolean) -> Unit,
    onCancel: () -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onCreate: () -> Unit,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    val motionScheme = motionScheme

    val windowHeight = LocalWindowInfo.current.containerSize.height
    val maxSheetHeight =
        if (windowHeight > 0) with(LocalDensity.current) { (windowHeight * 0.75f).toDp() }
        else Dp.Unspecified

    Box(modifier.heightIn(max = maxSheetHeight)) {
        val scrollState = rememberScrollState()

        LaunchedEffect(step) { scrollState.scrollTo(0) }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(bottom = 96.dp)
        ) {
            AnimatedContent(step) { currentStep ->
                Text(
                    if (currentStep == CreateTopicStep.Appearance)
                        stringResource(Res.string.create_new_topic)
                    else name,
                    style = typography.titleLargeEmphasized,
                    fontFamily = LocalAppFonts.current.topBarTitle,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                )
            }

            AnimatedContent(
                step,
                transitionSpec = {
                    val forward = targetState.ordinal > initialState.ordinal
                    (slideInHorizontally(motionScheme.defaultSpatialSpec()) {
                        if (forward) it else -it
                    } + fadeIn(motionScheme.defaultEffectsSpec()))
                        .togetherWith(
                            slideOutHorizontally(motionScheme.defaultSpatialSpec()) {
                                if (forward) -it else it
                            } + fadeOut(motionScheme.defaultEffectsSpec())
                        )
                },
                modifier = Modifier.fillMaxWidth()
            ) { currentStep ->
                when (currentStep) {
                    CreateTopicStep.Appearance ->
                        TopicShapeColorPicker(
                            name = name,
                            onNameValueChange = onNameValueChange,
                            color = color,
                            shape = shape,
                            nameTaken = nameTaken,
                            onNameChange = {},
                            onColorChange = onColorChange,
                            onShapeChange = onShapeChange,
                            containerColor = containerColor,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                    CreateTopicStep.Timer ->
                        TopicTimerProperties(
                            topicId = -1L,
                            autostartNextSession = autostartNextSession,
                            dndEnabled = dndEnabled,
                            topicRunning = false,
                            inTimerScreen = true,
                            focusTimeInputFieldState = focusTimeInputFieldState,
                            shortBreakTimeInputFieldState = shortBreakTimeInputFieldState,
                            longBreakTimeInputFieldState = longBreakTimeInputFieldState,
                            sessionsSliderState = sessionsSliderState,
                            onAutostartNextSessionChange = onAutostartNextSessionChange,
                            onDndEnabledChange = onDndEnabledChange,
                            showEditButtons = false,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            colorScheme.surfaceContainer.copy(0.7f),
                            colorScheme.surfaceContainer
                        ),
                        start = Offset.Zero,
                        end = Offset(x = Offset.Zero.x, y = Offset.Infinite.y)
                    )
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            FilledTonalButton(
                onClick = {
                    if (step == CreateTopicStep.Appearance) onCancel() else onBack()
                },
                shapes = ButtonDefaults.shapes()
            ) {
                AnimatedContent(step) { currentStep ->
                    Text(
                        stringResource(
                            if (currentStep == CreateTopicStep.Appearance) Res.string.cancel
                            else Res.string.back
                        )
                    )
                }
            }
            Button(
                onClick = {
                    if (step == CreateTopicStep.Appearance) onNext() else onCreate()
                },
                enabled = if (step == CreateTopicStep.Appearance) nextEnabled else createEnabled,
                shapes = ButtonDefaults.shapes()
            ) {
                AnimatedContent(step) { currentStep ->
                    Icon(
                        painterResource(
                            if (currentStep == CreateTopicStep.Appearance) Res.drawable.keyboard_arrow_right
                            else Res.drawable.check
                        ),
                        null,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(4.dp))
                AnimatedContent(step) { currentStep ->
                    Text(
                        stringResource(
                            if (currentStep == CreateTopicStep.Appearance) Res.string.next
                            else Res.string.add_topic
                        )
                    )
                }
            }
        }
    }
}

private fun Long.toMinutes() = (this / (60 * 1000)).toString()

private fun TextFieldState.toMillis(fallback: Long) =
    text.toString().toLongOrNull()?.times(60 * 1000) ?: fallback

@OptIn(ExperimentalMaterial3Api::class)
@Preview(widthDp = 412, heightDp = 600)
@Composable
private fun CreateTopicSheetContentPreview() {
    var step by remember { mutableStateOf(CreateTopicStep.Appearance) }
    var name by remember { mutableStateOf("") }
    var color by remember { mutableStateOf(Color.White) }
    var shape by remember { mutableStateOf(TopicShape.COOKIE_7_SIDED) }
    var autostartNextSession by remember { mutableStateOf(false) }
    var dndEnabled by remember { mutableStateOf(false) }

    TomatoTheme(dynamicColor = false) {
        SeededTheme(color) {
            val colorScheme = colorScheme

            Surface(color = colorScheme.surfaceContainer, modifier = Modifier.fillMaxSize()) {
                CreateTopicSheetContent(
                    step = step,
                    name = name,
                    onNameValueChange = { name = it },
                    color = color,
                    shape = shape,
                    nameTaken = false,
                    focusTimeInputFieldState = rememberTextFieldState("25"),
                    shortBreakTimeInputFieldState = rememberTextFieldState("5"),
                    longBreakTimeInputFieldState = rememberTextFieldState("15"),
                    sessionsSliderState = rememberSliderState(
                        value = 4f,
                        steps = 8,
                        valueRange = 1f..10f
                    ),
                    autostartNextSession = autostartNextSession,
                    dndEnabled = dndEnabled,
                    nextEnabled = name.isNotBlank(),
                    createEnabled = name.isNotBlank(),
                    onColorChange = { color = it },
                    onShapeChange = { shape = it },
                    onAutostartNextSessionChange = { autostartNextSession = it },
                    onDndEnabledChange = { dndEnabled = it },
                    onCancel = {},
                    onNext = { step = CreateTopicStep.Timer },
                    onBack = { step = CreateTopicStep.Appearance },
                    onCreate = {},
                    containerColor = colorScheme.surfaceContainer,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
