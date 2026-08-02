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

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalGridApi
import androidx.compose.foundation.layout.Grid
import androidx.compose.foundation.layout.GridTrackSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.checked
import androidx.compose.foundation.style.rememberUpdatedStyleState
import androidx.compose.foundation.style.styleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import org.nsh07.pomodoro.data.Topic
import org.nsh07.pomodoro.data.TopicShape
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsAction
import org.nsh07.pomodoro.ui.theme.SeededTheme

private const val shapeGridColumns = 5
private val shapeGridRows =
    (TopicShape.entries.size + shapeGridColumns - 1) / shapeGridColumns

private val topicColors = listOf(
    Color(0xfffeb4a7), Color(0xffffb3c0), Color(0xfffcaaff), Color(0xffb9c3ff),
    Color(0xff62d3ff), Color(0xff44d9f1), Color(0xff52dbc9), Color(0xff78dd77),
    Color(0xff9fd75c), Color(0xffc1d02d), Color(0xfffabd00), Color(0xffffb86e)
)

/**
 * A bottom sheet that lets the user pick the color and the shape of [topic].
 */
@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalFoundationStyleApi::class
)
@Composable
fun TopicShapeColorBottomSheet(
    topic: Topic,
    setShowSheet: (Boolean) -> Unit,
    onAction: (SettingsAction) -> Unit,
    modifier: Modifier = Modifier
) {
    SeededTheme(topic.color) {
        val colorScheme = MaterialTheme.colorScheme
        val motionScheme = MaterialTheme.motionScheme

        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onDismissRequest = { setShowSheet(false) },
            containerColor = colorScheme.surfaceContainer,
            modifier = modifier
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Color",
                    style = typography.labelLarge,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp)
                )
                ColorPickerRow(
                    topic.color,
                    topicColors,
                    isPlus = true,
                    onColorChange = { onAction(SettingsAction.SetEditingTopicColor(it)) },
                    backgroundColor = colorScheme.surfaceContainer,
                    horizontalPadding = 16.dp
                )

                Text(
                    "Shape",
                    style = typography.labelLarge,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                )
                @OptIn(ExperimentalGridApi::class)
                Grid(
                    config = {
                        repeat(shapeGridColumns) { column(1.fr) }
                        repeat(shapeGridRows) { row(GridTrackSize.Auto) }
                        gap(2.dp)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    TopicShape.entries.fastForEachIndexed { index, topicShape ->
                        val checked = topic.shape == topicShape
                        val styleState = rememberUpdatedStyleState(null) {
                            it.isChecked = checked
                        }
                        val rotation = animateFloatAsState(
                            if (checked) 360f else 0f,
                            animationSpec = motionScheme.slowSpatialSpec()
                        )
                        val shape = topicShape.toShape()

                        // Only the corners lying on the grid's outer edge are fully rounded,
                        // the ones facing a neighbouring cell stay tight
                        val row = index / shapeGridColumns
                        val column = index % shapeGridColumns
                        val isFirstRow = row == 0
                        val isLastRow = row == shapeGridRows - 1
                        val isFirstColumn = column == 0
                        val isLastColumn =
                            column == shapeGridColumns - 1 || index == TopicShape.entries.lastIndex
                        val cellShape = RoundedCornerShape(
                            topStart = if (isFirstRow && isFirstColumn) 16.dp else 4.dp,
                            topEnd = if (isFirstRow && isLastColumn) 16.dp else 4.dp,
                            bottomStart = if (isLastRow && isFirstColumn) 16.dp else 4.dp,
                            bottomEnd = if (isLastRow && isLastColumn) 16.dp else 4.dp
                        )

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .styleable(styleState) {
                                    shape(cellShape)
                                    background(colorScheme.surfaceBright)
                                    clip()
                                    checked {
                                        animate(motionScheme.slowSpatialSpec()) {
                                            shape(CircleShape)
                                        }
                                        animate(motionScheme.slowEffectsSpec()) {
                                            background(colorScheme.primaryContainer)
                                        }
                                    }
                                }
                                .clickable {
                                    onAction(SettingsAction.SetEditingTopicShape(topicShape))
                                }
                        ) {
                            Box(
                                Modifier
                                    .padding(16.dp)
                                    .fillMaxSize()
                                    .graphicsLayer { rotationZ = rotation.value }
                                    .styleable(styleState) {
                                        shape(shape)
                                        border(2.dp, colorScheme.primary)
                                        background(colorScheme.surfaceContainer)
                                        checked {
                                            animate { borderColor(colorScheme.onPrimaryContainer) }
                                        }
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}
