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

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.lerp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import com.materialkolor.ktx.harmonize
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.nsh07.pomodoro.data.Topic
import org.nsh07.pomodoro.data.Topic.Companion.defaultTopic
import org.nsh07.pomodoro.data.TopicShape
import org.nsh07.pomodoro.ui.mergePaddingValues
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsAction
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
import tomato.shared.generated.resources.edit
import tomato.shared.generated.resources.google_sans_flex
import tomato.shared.generated.resources.minutes_format
import tomato.shared.generated.resources.settings

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TopicsSettings(
    topics: List<Topic>,
    editingTopic: Topic,
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onAction: (SettingsAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val colorScheme = colorScheme

    val unselectedFont = typography.titleLargeEmphasized.copy(
        fontFamily = FontFamily(
            Font(
                Res.font.google_sans_flex,
                variationSettings = FontVariation.Settings(
                    FontVariation.weight(600),
                    FontVariation.width(100f),
                    FontVariation.Setting("ROND", 100f)
                )
            )
        )
    )

    val selectedFont = typography.titleLargeEmphasized.copy(
        fontFamily = FontFamily(
            Font(
                Res.font.google_sans_flex,
                variationSettings = FontVariation.Settings(
                    FontVariation.weight(900),
                    FontVariation.width(112.5f),
                    FontVariation.Setting("ROND", 35f)
                )
            )
        )
    )

    val widthExpanded = currentWindowAdaptiveInfo()
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

            SharedTransitionLayout {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    contentPadding = insets,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    itemsIndexed(topics, key = { _, topic -> topic.id }) { index, topic ->
                        val selected = editingTopic.id == topic.id
                        val primary = remember(topic.color) {
                            topic.color.let {
                                if (it == Color.White) colorScheme.primary
                                else it.harmonize(colorScheme.primary, true)
                            }
                        }
                        val onPrimary = remember(topic.color) {
                            topic.color.let {
                                if (it == Color.White) colorScheme.onPrimary
                                else it.harmonize(colorScheme.onPrimary, true)
                            }
                        }
                        val primaryContainer = remember(topic.color) {
                            topic.color.let {
                                if (it == Color.White) colorScheme.primaryContainer
                                else it.harmonize(colorScheme.primaryContainer, true)
                            }
                        }
                        val onPrimaryContainer = remember(topic.color) {
                            topic.color.let {
                                if (it == Color.White) colorScheme.onPrimaryContainer
                                else it.harmonize(colorScheme.onPrimaryContainer, true)
                            }
                        }
                        val surfaceBright = remember(topic.color) {
                            topic.color.let {
                                if (it == Color.White) colorScheme.surfaceBright
                                else it.harmonize(colorScheme.surfaceBright, true)
                            }
                        }
                        val surfaceContainer = remember(topic.color) {
                            topic.color.let {
                                if (it == Color.White) colorScheme.surfaceContainer
                                else it.harmonize(colorScheme.surfaceContainer, true)
                            }
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
                            onCheckedChange = {
                                onAction(SettingsAction.SetEditingTopic(topic))
                            },
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
                                        .padding(vertical = 4.dp)
                                        .size(72.dp)
                                        .background(
                                            animateColorAsState(
                                                if (!selected) primaryContainer
                                                else primary
                                            ).value,
                                            CircleShape
                                        )
                                ) {
                                    Box(
                                        Modifier
                                            .size(40.dp)
                                            .background(
                                                animateColorAsState(
                                                    if (!selected) primary
                                                    else onPrimary
                                                ).value,
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
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun TopicsSettingsPreview() {
    var editingTopic by remember {
        mutableStateOf(sampleTopics[5])
    }
    TomatoTheme(dynamicColor = false) {
        TopicsSettings(
            topics = sampleTopics,
            editingTopic = editingTopic,
            contentPadding = PaddingValues(0.dp),
            onBack = {},
            onAction = { editingTopic = (it as SettingsAction.SetEditingTopic).topic }
        )
    }
}

@Preview
@Composable
fun TopicsSettingsDarkPreview() {
    var editingTopic by remember {
        mutableStateOf(sampleTopics[5])
    }
    TomatoTheme(dynamicColor = false, darkTheme = true) {
        TopicsSettings(
            topics = sampleTopics,
            editingTopic = editingTopic,
            contentPadding = PaddingValues(0.dp),
            onBack = {},
            onAction = { editingTopic = (it as SettingsAction.SetEditingTopic).topic }
        )
    }
}

private val sampleTopics = listOf(
    defaultTopic,
    defaultTopic.copy(
        id = "work",
        name = "Work",
        color = Color(0xFF2196F3),
        shape = TopicShape.SQUARE
    ),
    defaultTopic.copy(
        id = "study",
        name = "Study",
        color = Color(0xFF4CAF50),
        shape = TopicShape.TRIANGLE
    ),
    defaultTopic.copy(
        id = "fitness",
        name = "Fitness",
        color = Color(0xFFF44336),
        shape = TopicShape.CIRCLE
    ),
    defaultTopic.copy(
        id = "coding",
        name = "Coding",
        color = Color(0xFF9C27B0),
        shape = TopicShape.DIAMOND
    ),
    defaultTopic.copy(
        id = "reading",
        name = "Reading",
        color = Color(0xFFFF9800),
        shape = TopicShape.PENTAGON
    ),
    defaultTopic.copy(
        id = "meditation",
        name = "Meditation",
        color = Color(0xFF00BCD4),
        shape = TopicShape.SUNNY
    ),
    defaultTopic.copy(
        id = "gaming",
        name = "Gaming",
        color = Color(0xFF795548),
        shape = TopicShape.BOOM
    ),
    defaultTopic.copy(
        id = "chores",
        name = "Chores",
        color = Color(0xFF607D8B),
        shape = TopicShape.FLOWER
    ),
    defaultTopic.copy(
        id = "music",
        name = "Music",
        color = Color(0xFFFFE082),
        shape = TopicShape.HEART
    ),
    defaultTopic.copy(
        id = "travel",
        name = "Travel",
        color = Color(0xFF8BC34A),
        shape = TopicShape.PILL
    )
)
