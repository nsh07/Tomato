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

package org.nsh07.pomodoro.ui.settingsScreen.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.animateBounds
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import com.materialkolor.ktx.harmonize
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.nsh07.pomodoro.data.Topic
import org.nsh07.pomodoro.data.TopicShape
import org.nsh07.pomodoro.ui.mergePaddingValues
import org.nsh07.pomodoro.ui.theme.CustomColors.detailPaneTopBarColors
import org.nsh07.pomodoro.ui.theme.CustomColors.listItemColors
import org.nsh07.pomodoro.ui.theme.CustomColors.topBarColors
import org.nsh07.pomodoro.ui.theme.LocalAppFonts
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.PANE_MAX_WIDTH
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.segmentedListItemShapes
import org.nsh07.pomodoro.ui.theme.TomatoTheme
import org.nsh07.pomodoro.ui.topBarWindowInsets
import tomato.shared.generated.resources.Res
import tomato.shared.generated.resources.arrow_back
import tomato.shared.generated.resources.back
import tomato.shared.generated.resources.grid_view
import tomato.shared.generated.resources.list_view
import tomato.shared.generated.resources.minutes_format
import tomato.shared.generated.resources.settings

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TopicsSettings(
    topics: List<Topic>,
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val colorScheme = colorScheme

    val widthExpanded = currentWindowAdaptiveInfo()
        .windowSizeClass
        .isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)

    val barColors = if (widthExpanded) detailPaneTopBarColors
    else topBarColors

    var grid by remember { mutableStateOf(false) }

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
                            "Topics",
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
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                        ) {
                            ToggleButton(
                                checked = !grid,
                                onCheckedChange = { grid = false },
                                contentPadding = PaddingValues(),
                                modifier = Modifier
                                    .size(IconButtonDefaults.smallContainerSize())
                                    .semantics { role = Role.RadioButton },
                                shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
                                colors = ToggleButtonDefaults.tonalToggleButtonColors()
                            ) {
                                Icon(
                                    painterResource(Res.drawable.list_view),
                                    null,
                                    modifier = Modifier.size(IconButtonDefaults.smallIconSize)
                                )
                            }
                            ToggleButton(
                                checked = grid,
                                onCheckedChange = { grid = true },
                                contentPadding = PaddingValues(),
                                modifier = Modifier
                                    .size(IconButtonDefaults.smallContainerSize())
                                    .semantics { role = Role.RadioButton },
                                shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
                                colors = ToggleButtonDefaults.tonalToggleButtonColors()
                            ) {
                                Icon(
                                    painterResource(Res.drawable.grid_view),
                                    null,
                                    modifier = Modifier.size(IconButtonDefaults.smallIconSize)
                                )
                            }
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
            var selectedId by remember { mutableStateOf("") }
            val appFonts = LocalAppFonts.current
            val minFormat = stringResource(Res.string.minutes_format)

            SharedTransitionLayout {
                AnimatedContent(grid) {
                    if (!it)
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            contentPadding = insets,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        ) {
                            itemsIndexed(topics, key = { _, topic -> topic.id }) { index, topic ->
                                val selected = selectedId == topic.id
                                val primary = remember(topic.color) {
                                    topic.color.let {
                                        if (it == Color.White) colorScheme.primary
                                        else it.harmonize(colorScheme.primary, true)
                                    }
                                }
                                val onPrimaryContainer = remember(topic.color) {
                                    topic.color.let {
                                        if (it == Color.White) colorScheme.onPrimaryContainer
                                        else it.harmonize(colorScheme.onPrimaryContainer, true)
                                    }
                                }
                                val primaryContainer = remember(topic.color) {
                                    topic.color.let {
                                        if (it == Color.White) colorScheme.primaryContainer
                                        else it.harmonize(colorScheme.primaryContainer, true)
                                    }
                                }

                                val surfaceBright = remember(topic.color) {
                                    topic.color.let {
                                        if (it == Color.White) colorScheme.surfaceBright
                                        else it.harmonize(colorScheme.surfaceBright, true)
                                    }
                                }

                                SegmentedListItem(
                                    checked = selected,
                                    onCheckedChange = {
                                        selectedId = if (it) topic.id else ""
                                    },
                                    shapes = segmentedListItemShapes(index, topics.size),
                                    colors = listItemColors.copy(containerColor = surfaceBright),
                                    verticalAlignment = Alignment.CenterVertically,
                                    leadingContent = {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .padding(vertical = 4.dp)
                                                .size(72.dp)
                                                .background(primaryContainer, CircleShape)
                                        ) {
                                            Box(
                                                Modifier
                                                    .size(40.dp)
                                                    .background(
                                                        onPrimaryContainer,
                                                        topic.shape.toShape()
                                                    )
                                            )
                                        }
                                    },
                                    supportingContent = {
                                        Text(
                                            "${
                                                String.format(
                                                    minFormat,
                                                    topic.focusTime / 60000
                                                )
                                            } / ${
                                                String.format(
                                                    minFormat,
                                                    topic.shortBreakTime / 60000
                                                )
                                            } / ${
                                                String.format(
                                                    minFormat,
                                                    topic.longBreakTime / 60000
                                                )
                                            }, ${topic.sessionLength} timers",
                                            style = typography.labelLarge,
                                            color = colorScheme.onSecondaryContainer
                                        )
                                    }
                                ) {
                                    Text(
                                        topic.name,
                                        style = typography.titleLargeEmphasized,
                                        fontFamily = appFonts.topBarTitle
                                    )
                                }
                            }
                        }
                    else LazyVerticalGrid(
                        columns = GridCells.Adaptive(150.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = insets,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        items(
                            topics,
                            key = { it.id },
                            span = {
                                if (it.id == selectedId) GridItemSpan(maxLineSpan)
                                else GridItemSpan(1)
                            }
                        ) { topic ->
                            val color = remember(topic.color) {
                                topic.color.let {
                                    if (it != Color.White) it.harmonize(
                                        colorScheme.primaryContainer,
                                        true
                                    )
                                    else colorScheme.primaryContainer
                                }
                            }

                            val selected = selectedId == topic.id
                            Box(
                                Modifier
                                    .animateItem()
                                    .animateBounds(this@SharedTransitionLayout)
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clip(shapes.extraExtraLarge)
                                    .background(listItemColors.containerColor)
                                    .clickable { selectedId = if (!selected) topic.id else "" }
                            ) {
                                Box(
                                    Modifier
                                        .requiredSize(300.dp)
                                        .offset((-75).dp, (-75).dp)
                                        .background(color, topic.shape.toShape())
                                )
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        topic.name,
                                        style = typography.titleLarge,
                                        fontFamily = appFonts.topBarTitle,
                                        color = colorScheme.onSurface
                                    )
                                    Text(
                                        "${topic.focusTime / 60000}/${topic.shortBreakTime / 60000}/${topic.longBreakTime / 60000}",
                                        style = typography.labelLarge,
                                        color = colorScheme.onSecondaryContainer
                                    )
                                }
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
    TomatoTheme(dynamicColor = false) {
        TopicsSettings(
            topics = sampleTopics,
            contentPadding = PaddingValues(0.dp),
            onBack = {}
        )
    }
}

@Preview
@Composable
fun TopicsSettingsDarkPreview() {
    TomatoTheme(darkTheme = true, dynamicColor = false) {
        TopicsSettings(
            topics = sampleTopics,
            contentPadding = PaddingValues(0.dp),
            onBack = {}
        )
    }
}

private val sampleTopics = listOf(
    Topic.defaultTopic,
    Topic.defaultTopic.copy(
        id = "work",
        name = "Work",
        color = Color(0xFF2196F3),
        shape = TopicShape.SQUARE
    ),
    Topic.defaultTopic.copy(
        id = "study",
        name = "Study",
        color = Color(0xFF4CAF50),
        shape = TopicShape.TRIANGLE
    ),
    Topic.defaultTopic.copy(
        id = "fitness",
        name = "Fitness",
        color = Color(0xFFF44336),
        shape = TopicShape.CIRCLE
    ),
    Topic.defaultTopic.copy(
        id = "coding",
        name = "Coding",
        color = Color(0xFF9C27B0),
        shape = TopicShape.DIAMOND
    ),
    Topic.defaultTopic.copy(
        id = "reading",
        name = "Reading",
        color = Color(0xFFFF9800),
        shape = TopicShape.PENTAGON
    ),
    Topic.defaultTopic.copy(
        id = "meditation",
        name = "Meditation",
        color = Color(0xFF00BCD4),
        shape = TopicShape.SUNNY
    ),
    Topic.defaultTopic.copy(
        id = "gaming",
        name = "Gaming",
        color = Color(0xFF795548),
        shape = TopicShape.BOOM
    ),
    Topic.defaultTopic.copy(
        id = "chores",
        name = "Chores",
        color = Color(0xFF607D8B),
        shape = TopicShape.FLOWER
    ),
    Topic.defaultTopic.copy(
        id = "music",
        name = "Music",
        color = Color(0xFFFFE082),
        shape = TopicShape.HEART
    ),
    Topic.defaultTopic.copy(
        id = "travel",
        name = "Travel",
        color = Color(0xFF8BC34A),
        shape = TopicShape.PILL
    )
)
