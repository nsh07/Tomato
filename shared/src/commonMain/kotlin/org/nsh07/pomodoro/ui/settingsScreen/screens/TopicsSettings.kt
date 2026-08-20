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

@file:OptIn(ExperimentalFoundationStyleApi::class)

package org.nsh07.pomodoro.ui.settingsScreen.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.MutableStyleState
import androidx.compose.foundation.style.StyleScope
import androidx.compose.foundation.style.StyleStateKey
import androidx.compose.foundation.style.animate
import androidx.compose.foundation.style.externalPaddingVertical
import androidx.compose.foundation.style.size
import androidx.compose.foundation.style.styleable
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.rememberSliderState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.lerp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import com.materialkolor.ktx.harmonize
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.nsh07.pomodoro.data.Topic
import org.nsh07.pomodoro.data.TopicShape
import org.nsh07.pomodoro.ui.mergePaddingValues
import org.nsh07.pomodoro.ui.settingsScreen.components.TopicShapeColorPicker
import org.nsh07.pomodoro.ui.settingsScreen.components.TopicTimerSettings
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsAction
import org.nsh07.pomodoro.ui.theme.CustomColors.detailPaneTopBarColors
import org.nsh07.pomodoro.ui.theme.CustomColors.listItemColors
import org.nsh07.pomodoro.ui.theme.CustomColors.topBarColors
import org.nsh07.pomodoro.ui.theme.LocalAppFonts
import org.nsh07.pomodoro.ui.theme.SeededTheme
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.PANE_MAX_WIDTH
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.segmentedListItemShapes
import org.nsh07.pomodoro.ui.theme.TomatoTheme
import org.nsh07.pomodoro.ui.topBarWindowInsets
import tomato.shared.generated.resources.Res
import tomato.shared.generated.resources.add
import tomato.shared.generated.resources.add_topic
import tomato.shared.generated.resources.arrow_back
import tomato.shared.generated.resources.back
import tomato.shared.generated.resources.cancel
import tomato.shared.generated.resources.create_new_topic
import tomato.shared.generated.resources.edit
import tomato.shared.generated.resources.minutes_format
import tomato.shared.generated.resources.settings
import tomato.shared.generated.resources.topic_summary_format
import tomato.shared.generated.resources.topics
import kotlin.math.PI
import kotlin.math.cos

val selectedKey = StyleStateKey(false)

var MutableStyleState.selected: Boolean
    get() = this[selectedKey]
    set(value) {
        this[selectedKey] = value
    }

fun StyleScope.selected(block: () -> Unit) {
    state(selectedKey, block) { key, state -> state[key] }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TopicsSettings(
    topics: List<Topic>,
    editingTopic: Topic,
    serviceRunning: Boolean,
    currentTopicId: Long,
    focusTimeInputFieldState: TextFieldState,
    shortBreakTimeInputFieldState: TextFieldState,
    longBreakTimeInputFieldState: TextFieldState,
    sessionsSliderState: SliderState,
    contentPadding: PaddingValues,
    isPlus: Boolean,
    setShowPaywall: (Boolean) -> Unit,
    onBack: () -> Unit,
    onAction: (SettingsAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val defaultTopicColor = colorScheme.primary

    var creatingTopic by remember { mutableStateOf(false) }
    var newTopicName by remember { mutableStateOf("") }
    var newTopicColor by remember { mutableStateOf(Color.White) }
    var newTopicShape by remember { mutableStateOf(Topic.defaultTopic.shape) }

    fun resetNewTopic() {
        newTopicName = ""
        newTopicColor = Color.White
        newTopicShape = Topic.defaultTopic.shape
        creatingTopic = false
    }

    // While a topic is being created, the screen follows the color picked for the new topic
    SeededTheme(if (creatingTopic) newTopicColor else editingTopic.color) {
        val colorScheme = colorScheme
        val motionScheme = motionScheme
        val shapes = shapes
        val topBarTitle = LocalAppFonts.current.topBarTitle

        val unselectedFont = typography.titleLargeEmphasized.copy(
            fontFamily = typography.bodyLarge.fontFamily
        )

        val selectedFont = typography.titleLargeEmphasized.copy(
            fontFamily = topBarTitle
        )

        val widthExpanded = currentWindowAdaptiveInfoV2()
            .windowSizeClass
            .isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)

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
                                stringResource(Res.string.topics),
                                fontFamily = topBarTitle
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
                val minFormat = stringResource(Res.string.minutes_format)
                val summaryFormat = stringResource(Res.string.topic_summary_format)
                val lazyColumnState = rememberLazyListState()
                SharedTransitionLayout {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        contentPadding = insets,
                        state = lazyColumnState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            val styleState = remember { MutableStyleState(null) }
                            styleState.selected = creatingTopic

                            Box(
                                modifier = Modifier
                                    .padding(horizontal = if (creatingTopic) 0.dp else 16.dp)
                                    .styleable(styleState) {
                                        shape(RoundedCornerShape(40.dp))
                                        clip(true)
                                        background(colorScheme.primary)
                                        selected { animate { background(colorScheme.surfaceContainer) } }
                                    }
                                    .animateContentSize(motionScheme.slowSpatialSpec())
                                    .clickable { creatingTopic = !creatingTopic },
                                contentAlignment = Alignment.Center
                            ) {
                                val newTopicText = stringResource(Res.string.create_new_topic)
                                AnimatedContent(creatingTopic) {
                                    if (!it) Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .styleable {
                                                    size(40.dp)
                                                    shape(CircleShape)
                                                    background(colorScheme.onPrimary)
                                                }
                                        ) {
                                            val shape = MaterialShapes.Boom.toShape()
                                            Box(
                                                Modifier.styleable {
                                                    size(22.dp)
                                                    shape(shape)
                                                    background(colorScheme.onPrimaryContainer)
                                                }
                                            )
                                        }
                                        Text(
                                            newTopicText,
                                            style = typography.bodyLargeEmphasized,
                                            color = colorScheme.onPrimary,
                                            modifier = Modifier
                                                .sharedBounds(
                                                    rememberSharedContentState(newTopicText),
                                                    this@AnimatedContent
                                                )
                                                .weight(1f)
                                        )
                                        FilledIconButton(
                                            onClick = { creatingTopic = true },
                                            shapes = IconButtonDefaults.shapes(),
                                            colors = IconButtonDefaults.filledIconButtonColors(
                                                containerColor = colorScheme.primaryContainer
                                            ),
                                            modifier = Modifier.size(
                                                IconButtonDefaults.smallContainerSize(
                                                    IconButtonDefaults.IconButtonWidthOption.Wide
                                                )
                                            )
                                        ) { Icon(painterResource(Res.drawable.add), null) }
                                    }
                                    else Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            newTopicText,
                                            style = typography.titleLargeEmphasized,
                                            fontFamily = topBarTitle,
                                            modifier = Modifier
                                                .sharedBounds(
                                                    rememberSharedContentState(newTopicText),
                                                    this@AnimatedContent
                                                )
                                                .padding(
                                                    start = 16.dp,
                                                    top = 24.dp,
                                                    end = 16.dp
                                                )
                                        )
                                        val trimmedNewTopicName = newTopicName.trim()
                                        val nameTaken = remember(trimmedNewTopicName, topics) {
                                            topics.any {
                                                it.name.equals(
                                                    trimmedNewTopicName,
                                                    true
                                                )
                                            }
                                        }

                                        TopicShapeColorPicker(
                                            name = newTopicName,
                                            onNameValueChange = { newTopicName = it },
                                            color = newTopicColor,
                                            shape = newTopicShape,
                                            nameTaken = nameTaken,
                                            onNameChange = {},
                                            onColorChange = { newTopicColor = it },
                                            onShapeChange = { newTopicShape = it },
                                            isPlus = isPlus,
                                            setShowPaywall = setShowPaywall,
                                            containerColor = colorScheme.surfaceContainer
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(
                                                8.dp,
                                                Alignment.End
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp)
                                        ) {
                                            TextButton(onClick = { resetNewTopic() }) {
                                                Text(stringResource(Res.string.cancel))
                                            }
                                            Button(
                                                onClick = {
                                                    onAction(
                                                        SettingsAction.CreateTopic(
                                                            Topic.defaultTopic.copy(
                                                                id = 0,
                                                                name = trimmedNewTopicName,
                                                                color = newTopicColor,
                                                                shape = newTopicShape
                                                            )
                                                        )
                                                    )
                                                    resetNewTopic()
                                                },
                                                enabled = trimmedNewTopicName.isNotEmpty() && !nameTaken
                                            ) {
                                                Text(stringResource(Res.string.add_topic))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        item {
                            val dividerColor = colorScheme.primary
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 36.dp, vertical = 20.dp)
                                    .height(12.dp)
                            ) {
                                val wavelengthPx = 20.dp.toPx()
                                val amplitudePx = 2.dp.toPx()
                                val strokeWidthPx = 4.dp.toPx()
                                val centerY = size.height / 2f

                                val path = Path().apply {
                                    moveTo(0f, centerY + amplitudePx)
                                    var x = 0f
                                    while (x < size.width) {
                                        x += 2f
                                        val y = centerY +
                                                amplitudePx * cos(2 * PI * x / wavelengthPx).toFloat()
                                        lineTo(x, y)
                                    }
                                }

                                drawPath(
                                    path = path,
                                    color = dividerColor,
                                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                                )
                            }
                        }
                        itemsIndexed(topics, key = { _, topic -> topic.id }) { index, topic ->
                            val selected = topic.id == editingTopic.id && !creatingTopic
                            val shape = topic.shape.toShape()

                            val styleState = remember { MutableStyleState(null) }
                            styleState.selected = selected

                            val topicColor =
                                if (topic.color == Color.White) defaultTopicColor
                                else topic.color

                            val primary = remember(topicColor, colorScheme.primary) {
                                topicColor.harmonize(colorScheme.primary, true)
                            }
                            val onPrimary = remember(topicColor, colorScheme.onPrimary) {
                                topicColor.harmonize(colorScheme.onPrimary, true)
                            }
                            val primaryContainer =
                                remember(topicColor, colorScheme.primaryContainer) {
                                    topicColor.harmonize(colorScheme.primaryContainer, true)
                                }
                            val onPrimaryContainer =
                                remember(topicColor, colorScheme.onPrimaryContainer) {
                                    topicColor.harmonize(colorScheme.onPrimaryContainer, true)
                                }
                            val surfaceBright = remember(topicColor, colorScheme.surfaceBright) {
                                topicColor.harmonize(colorScheme.surfaceBright, true)
                            }
                            val surfaceContainer =
                                remember(topicColor, colorScheme.surfaceContainer) {
                                    topicColor.harmonize(colorScheme.surfaceContainer, true)
                                }

                            val progress by animateFloatAsState(
                                if (selected) 1f else 0f,
                                animationSpec = motionScheme.defaultEffectsSpec()
                            )
                            val titleFontFamily: TextStyle by remember(progress) {
                                derivedStateOf {
                                    lerp(unselectedFont, selectedFont, progress)
                                }
                            }

                            SegmentedListItem(
                                checked = selected,
                                onCheckedChange = { onAction(SettingsAction.SetEditingTopic(topic)) },
                                shapes = segmentedListItemShapes(
                                    index,
                                    topics.size
                                ),
                                colors = listItemColors.copy(
                                    containerColor = surfaceBright,
                                    selectedContainerColor = primaryContainer
                                ),
                                verticalAlignment = Alignment.CenterVertically,
                                leadingContent = {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .styleable(styleState) {
                                                externalPaddingVertical(4.dp)
                                                size(72.dp)
                                                shape(CircleShape)
                                                background(primaryContainer)
                                                selected { animate { background(primary) } }
                                            }
                                    ) {
                                        Box(
                                            Modifier
                                                .styleable(styleState) {
                                                    size(40.dp)
                                                    shape(shape)
                                                    background(primary)
                                                    selected { animate { background(onPrimary) } }
                                                }
                                        )
                                    }
                                },
                                supportingContent = {
                                    Text(
                                        String.format(
                                            summaryFormat,
                                            String.format(minFormat, topic.focusTime / 60000),
                                            String.format(minFormat, topic.shortBreakTime / 60000),
                                            String.format(minFormat, topic.longBreakTime / 60000),
                                            topic.sessionLength
                                        ),
                                        style = typography.labelLarge,
                                        color = colorScheme.onSecondaryContainer
                                    )
                                },
                                trailingContent = {
                                    FilledIconToggleButton(
                                        checked = selected,
                                        onCheckedChange = {
                                            onAction(SettingsAction.SetEditingTopic(topic))
                                        },
                                        colors = IconButtonDefaults.filledIconToggleButtonColors(
                                            containerColor = surfaceContainer,
                                            checkedContainerColor = primary,
                                            checkedContentColor = onPrimary
                                        ),
                                        shapes = IconButtonDefaults.toggleableShapes(checkedShape = shapes.large),
                                        modifier = Modifier.size(IconButtonDefaults.mediumContainerSize())
                                    ) {
                                        Icon(
                                            painterResource(Res.drawable.edit),
                                            null,
                                            modifier = Modifier.size(IconButtonDefaults.mediumIconSize)
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .styleable(styleState) {
                                        externalPaddingTop(0.dp)
                                        selected { animate { externalPaddingTop(2.dp) } }
                                    }
                            ) {
                                Text(
                                    topic.name,
                                    style = titleFontFamily,
                                    color = animateColorAsState(
                                        if (!selected) colorScheme.onSurface
                                        else onPrimaryContainer
                                    ).value
                                )
                            }

                            AnimatedVisibility(
                                selected,
                                enter = expandVertically(motionScheme.slowSpatialSpec()),
                                exit = shrinkVertically(motionScheme.slowSpatialSpec())
                            ) {
                                TopicTimerSettings(
                                    topic = topic,
                                    topics = topics,
                                    topicRunning = serviceRunning && topic.id == currentTopicId,
                                    focusTimeInputFieldState = focusTimeInputFieldState,
                                    shortBreakTimeInputFieldState = shortBreakTimeInputFieldState,
                                    longBreakTimeInputFieldState = longBreakTimeInputFieldState,
                                    sessionsSliderState = sessionsSliderState,
                                    onAction = onAction,
                                    isPlus = isPlus,
                                    setShowPaywall = setShowPaywall,
                                    modifier = Modifier.padding(
                                        start = 16.dp,
                                        top = 2.dp,
                                        end = 16.dp,
                                        bottom = 2.dp
                                    ),
                                    inTimerScreen = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun TopicsSettingsPreview() {
    var topics by remember { mutableStateOf(sampleTopics) }
    var editingTopic by remember {
        mutableStateOf(sampleTopics[5])
    }
    TomatoTheme(dynamicColor = false) {
        TopicsSettings(
            topics = topics,
            editingTopic = editingTopic,
            serviceRunning = false,
            currentTopicId = editingTopic.id,
            focusTimeInputFieldState = TextFieldState("25"),
            shortBreakTimeInputFieldState = TextFieldState("5"),
            longBreakTimeInputFieldState = TextFieldState("15"),
            sessionsSliderState = rememberSliderState(4f, valueRange = 1f..10f),
            contentPadding = PaddingValues(0.dp),
            isPlus = false,
            setShowPaywall = {},
            onBack = {},
            onAction = { action ->
                when (action) {
                    is SettingsAction.SetEditingTopic -> editingTopic = action.topic
                    is SettingsAction.CreateTopic -> {
                        val topic = action.topic.copy(id = topics.maxOf { it.id } + 1)
                        topics = (topics + topic).sortedBy { it.name }
                        editingTopic = topic
                    }

                    else -> Unit
                }
            }
        )
    }
}

@Preview
@Composable
fun TopicsSettingsDarkPreview() {
    var topics by remember { mutableStateOf(sampleTopics) }
    var editingTopic by remember {
        mutableStateOf(sampleTopics[5])
    }
    TomatoTheme(darkTheme = true, dynamicColor = false) {
        TopicsSettings(
            topics = topics,
            editingTopic = editingTopic,
            serviceRunning = false,
            currentTopicId = editingTopic.id,
            focusTimeInputFieldState = TextFieldState("25"),
            shortBreakTimeInputFieldState = TextFieldState("5"),
            longBreakTimeInputFieldState = TextFieldState("15"),
            sessionsSliderState = rememberSliderState(4f, valueRange = 1f..10f),
            contentPadding = PaddingValues(0.dp),
            isPlus = false,
            setShowPaywall = {},
            onBack = {},
            onAction = { action ->
                when (action) {
                    is SettingsAction.SetEditingTopic -> editingTopic = action.topic
                    is SettingsAction.CreateTopic -> {
                        val topic = action.topic.copy(id = topics.maxOf { it.id } + 1)
                        topics = (topics + topic).sortedBy { it.name }
                        editingTopic = topic
                    }

                    else -> Unit
                }
            }
        )
    }
}

val sampleTopics = listOf(
    Topic(
        id = 1,
        name = "Default",
        color = Color.White,
        shape = TopicShape.COOKIE_12_SIDED,
        focusTime = 25 * 60000L,
        shortBreakTime = 5 * 60000L,
        longBreakTime = 15 * 60000L,
        sessionLength = 4,
        autostartNextSession = false,
        dndEnabled = false
    ),
    Topic(
        id = 2,
        name = "Work",
        color = Color(0xFF2196F3),
        shape = TopicShape.SQUARE,
        focusTime = 50 * 60000L,
        shortBreakTime = 10 * 60000L,
        longBreakTime = 30 * 60000L,
        sessionLength = 3,
        autostartNextSession = true,
        dndEnabled = true
    ),
    Topic(
        id = 3,
        name = "Study",
        color = Color(0xFF4CAF50),
        shape = TopicShape.TRIANGLE,
        focusTime = 45 * 60000L,
        shortBreakTime = 5 * 60000L,
        longBreakTime = 20 * 60000L,
        sessionLength = 4,
        autostartNextSession = false,
        dndEnabled = true
    ),
    Topic(
        id = 4,
        name = "Fitness",
        color = Color(0xFFF44336),
        shape = TopicShape.CIRCLE,
        focusTime = 30 * 60000L,
        shortBreakTime = 2 * 60000L,
        longBreakTime = 10 * 60000L,
        sessionLength = 6,
        autostartNextSession = true,
        dndEnabled = false
    ),
    Topic(
        id = 5,
        name = "Coding",
        color = Color(0xFF9C27B0),
        shape = TopicShape.DIAMOND,
        focusTime = 60 * 60000L,
        shortBreakTime = 10 * 60000L,
        longBreakTime = 40 * 60000L,
        sessionLength = 2,
        autostartNextSession = false,
        dndEnabled = true
    ),
    Topic(
        id = 6,
        name = "Reading",
        color = Color(0xFFFF9800),
        shape = TopicShape.PENTAGON,
        focusTime = 20 * 60000L,
        shortBreakTime = 3 * 60000L,
        longBreakTime = 15 * 60000L,
        sessionLength = 5,
        autostartNextSession = true,
        dndEnabled = false
    ),
    Topic(
        id = 7,
        name = "Meditation",
        color = Color(0xFF00BCD4),
        shape = TopicShape.SUNNY,
        focusTime = 15 * 60000L,
        shortBreakTime = 0,
        longBreakTime = 0,
        sessionLength = 1,
        autostartNextSession = false,
        dndEnabled = true
    ),
    Topic(
        id = 8,
        name = "Gaming",
        color = Color(0xFF795548),
        shape = TopicShape.BOOM,
        focusTime = 120 * 60000L,
        shortBreakTime = 15 * 60000L,
        longBreakTime = 60 * 60000L,
        sessionLength = 2,
        autostartNextSession = false,
        dndEnabled = false
    ),
    Topic(
        id = 9,
        name = "Chores",
        color = Color(0xFF607D8B),
        shape = TopicShape.FLOWER,
        focusTime = 10 * 60000L,
        shortBreakTime = 2 * 60000L,
        longBreakTime = 5 * 60000L,
        sessionLength = 10,
        autostartNextSession = true,
        dndEnabled = false
    ),
    Topic(
        id = 10,
        name = "Music",
        color = Color(0xFFFFE082),
        shape = TopicShape.HEART,
        focusTime = 35 * 60000L,
        shortBreakTime = 5 * 60000L,
        longBreakTime = 15 * 60000L,
        sessionLength = 4,
        autostartNextSession = false,
        dndEnabled = false
    ),
    Topic(
        id = 11,
        name = "Travel",
        color = Color(0xFF8BC34A),
        shape = TopicShape.PILL,
        focusTime = 40 * 60000L,
        shortBreakTime = 8 * 60000L,
        longBreakTime = 25 * 60000L,
        sessionLength = 3,
        autostartNextSession = true,
        dndEnabled = true
    )
)
