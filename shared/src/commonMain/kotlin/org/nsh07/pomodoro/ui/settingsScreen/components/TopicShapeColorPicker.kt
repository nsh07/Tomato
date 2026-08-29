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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import org.jetbrains.compose.resources.stringResource
import org.nsh07.pomodoro.data.TopicShape
import org.nsh07.pomodoro.ui.LocalIsPlus
import org.nsh07.pomodoro.ui.performSegmentTick
import tomato.shared.generated.resources.Res
import tomato.shared.generated.resources.topic_name
import tomato.shared.generated.resources.topic_name_taken
import kotlin.time.Duration.Companion.milliseconds

private const val shapeGridColumns = 5
private val shapeGridGap = 2.dp
private val shapeGridRows = TopicShape.entries.chunked(shapeGridColumns)

/** The number of trailing shape grid rows that are only available to Tomato Plus users. */
private const val plusShapeGridRows = 2

@OptIn(ExperimentalMaterial3ExpressiveApi::class, FlowPreview::class)
@Composable
fun TopicShapeColorPicker(
    name: String,
    onNameValueChange: (String) -> Unit,
    color: Color,
    shape: TopicShape,
    nameTaken: Boolean,
    onNameChange: (String) -> Unit,
    onColorChange: (Color) -> Unit,
    onShapeChange: (TopicShape) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = colorScheme.surfaceContainer,
    horizontalPadding: Dp = 16.dp
) {
    val isPlus = LocalIsPlus.current

    val currentName by rememberUpdatedState(name)
    val currentNameTaken by rememberUpdatedState(nameTaken)
    val currentOnNameChange by rememberUpdatedState(onNameChange)

    LaunchedEffect(Unit) {
        fun saveName(trimmedName: String) {
            if (trimmedName.isNotEmpty() && !currentNameTaken) currentOnNameChange(trimmedName)
        }

        try {
            snapshotFlow { currentName.trim() }
                .drop(1)
                .debounce(500.milliseconds)
                .collect { saveName(it) }
        } finally {
            // Persist an edit that is still waiting on the debounce when the picker goes away
            saveName(currentName.trim())
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        Text(
            stringResource(Res.string.topic_name),
            style = typography.labelLarge,
            color = colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = horizontalPadding)
        )
        TopicNameTextField(
            name = name,
            onNameChange = onNameValueChange,
            isError = nameTaken,
            supportingText = if (nameTaken) stringResource(Res.string.topic_name_taken) else null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding)
        )

        Text(
            "Color",
            style = typography.labelLarge,
            color = colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = horizontalPadding, top = 12.dp)
        )
        ColorPickerRow(
            color,
            onColorChange = onColorChange,
            enabled = true,
            backgroundColor = containerColor,
            horizontalPadding = horizontalPadding
        )

        Text(
            "Shape",
            style = typography.labelLarge,
            color = colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = horizontalPadding, top = 8.dp)
        )
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding)
                    .clip(RoundedCornerShape(28.dp))
            ) {
                val cellSize =
                    (maxWidth - shapeGridGap * (shapeGridColumns - 1)) / shapeGridColumns

                val firstPlusRow = shapeGridRows.size - plusShapeGridRows

                Column(verticalArrangement = Arrangement.spacedBy(shapeGridGap)) {
                    shapeGridRows.fastForEachIndexed { rowIndex, rowShapes ->
                        val rowEnabled = isPlus || rowIndex < firstPlusRow

                        if (!isPlus && rowIndex == firstPlusRow) PlusDivider(
                            backgroundColor = containerColor
                        )

                        val interactionSources =
                            remember { List(rowShapes.size) { MutableInteractionSource() } }

                        ButtonGroup(
                            overflowIndicator = {},
                            horizontalArrangement = Arrangement.spacedBy(shapeGridGap),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowShapes.fastForEachIndexed { index, topicShape ->
                                customItem(
                                    buttonGroupContent = {
                                        TopicShapeButton(
                                            topicShape = topicShape,
                                            checked = shape == topicShape,
                                            enabled = rowEnabled,
                                            interactionSource = interactionSources[index],
                                            onClick = { onShapeChange(topicShape) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(cellSize)
                                                .animateWidth(interactionSources[index])
                                        )
                                    },
                                    menuContent = {}
                                )
                            }

                            // Keep the last row aligned to the grid when it isn't completely filled
                            val emptyCells = shapeGridColumns - rowShapes.size
                            if (emptyCells > 0) customItem(
                                buttonGroupContent = {
                                    Spacer(Modifier.weight(emptyCells.toFloat()))
                                },
                                menuContent = {}
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TopicShapeButton(
    topicShape: TopicShape,
    checked: Boolean,
    enabled: Boolean,
    interactionSource: MutableInteractionSource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val pressed by interactionSource.collectIsPressedAsState()

    val rotation = animateFloatAsState(
        (if (checked) 360f else 0f) + (if (pressed) 90f else 0f),
        animationSpec = if (pressed) motionScheme.fastSpatialSpec()
        else motionScheme.slowSpatialSpec()
    )
    val checkedFraction = animateFloatAsState(
        if (checked) 1f else 0f,
        animationSpec = motionScheme.slowEffectsSpec()
    )

    val containerColor =
        lerp(colorScheme.secondaryContainer, colorScheme.primary, checkedFraction.value)
    val contentColor =
        lerp(colorScheme.onSecondaryContainer, colorScheme.onPrimary, checkedFraction.value)
    val strokeColor = colorScheme.secondary
    val shapeColor = colorScheme.onSecondary
    val checkedColor = colorScheme.onPrimary

    val cellContentShape = topicShape.toShape()

    FilledIconToggleButton(
        checked = checked,
        onCheckedChange = {
            if (!checked) haptic.performSegmentTick()
            onClick()
        },
        enabled = enabled,
        shapes = IconButtonDefaults.toggleableShapes(
            shape = shapes.small,
            pressedShape = shapes.large,
            checkedShape = CircleShape
        ),
        colors = IconButtonDefaults.filledIconToggleButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            checkedContainerColor = containerColor,
            checkedContentColor = contentColor
        ),
        interactionSource = interactionSource,
        modifier = modifier
    ) {
        Box(
            Modifier
                .padding(vertical = 16.dp)
                .aspectRatio(1f)
                .fillMaxHeight()
                .graphicsLayer { rotationZ = rotation.value }
                .drawWithCache { // shape
                    val strokeWidth = 2.dp.toPx()
                    val outline = cellContentShape.createOutline(
                        Size(
                            size.width - strokeWidth,
                            size.height - strokeWidth
                        ),
                        layoutDirection,
                        this
                    )
                    val stroke = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                    onDrawBehind {
                        val fraction = checkedFraction.value
                        translate(strokeWidth / 2f, strokeWidth / 2f) {
                            drawOutline(outline, lerp(shapeColor, checkedColor, fraction))
                            drawOutline(
                                outline,
                                lerp(strokeColor, Color.Transparent, fraction),
                                style = stroke
                            )
                        }
                    }
                }
        )
    }
}
