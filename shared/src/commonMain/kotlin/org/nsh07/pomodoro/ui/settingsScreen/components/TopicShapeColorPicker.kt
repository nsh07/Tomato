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

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalGridApi
import androidx.compose.foundation.layout.Grid
import androidx.compose.foundation.layout.GridTrackSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.animate
import androidx.compose.foundation.style.checked
import androidx.compose.foundation.style.rememberUpdatedStyleState
import androidx.compose.foundation.style.styleable
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.nsh07.pomodoro.data.TopicShape
import tomato.shared.generated.resources.Res
import tomato.shared.generated.resources.topic_name
import tomato.shared.generated.resources.topic_name_taken
import kotlin.time.Duration.Companion.milliseconds

private const val shapeGridColumns = 5
private val shapeGridRows =
    (TopicShape.entries.size + shapeGridColumns - 1) / shapeGridColumns

internal val topicColors = listOf(
    Color.White,
    Color(0xfffeb4a7), Color(0xffffb3c0), Color(0xfffcaaff), Color(0xffb9c3ff),
    Color(0xff62d3ff), Color(0xff44d9f1), Color(0xff52dbc9), Color(0xff78dd77),
    Color(0xff9fd75c), Color(0xffc1d02d), Color(0xfffabd00), Color(0xffffb86e)
)

@Stable
private class AnimatedCornerShape(initialShape: RoundedCornerShape) : Shape {
    private var shape = initialShape
    private var size = Size.Zero
    private var density = Density(1f)
    private var corners: Array<Animatable<Float, AnimationVector1D>>? = null

    suspend fun animateTo(target: RoundedCornerShape, spec: FiniteAnimationSpec<Float>) {
        shape = target
        val corners = corners ?: return
        coroutineScope {
            corners.forEachIndexed { index, corner ->
                launch { corner.animateTo(target.cornerPx(index), spec) }
            }
        }
    }

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        this.size = size
        this.density = density
        val corners = corners ?: Array(4) { Animatable(shape.cornerPx(it)) }.also { corners = it }
        val maxCorner = size.minDimension / 2f
        return shape
            .copy(
                topStart = CornerSize(corners[0].value.coerceAtMost(maxCorner)),
                topEnd = CornerSize(corners[1].value.coerceAtMost(maxCorner)),
                bottomEnd = CornerSize(corners[2].value.coerceAtMost(maxCorner)),
                bottomStart = CornerSize(corners[3].value.coerceAtMost(maxCorner))
            )
            .createOutline(size, layoutDirection, density)
    }

    private fun RoundedCornerShape.cornerPx(index: Int) = when (index) {
        0 -> topStart
        1 -> topEnd
        2 -> bottomEnd
        else -> bottomStart
    }.toPx(size, density)
}

@OptIn(
    ExperimentalMaterial3ExpressiveApi::class, ExperimentalFoundationStyleApi::class,
    FlowPreview::class
)
@Composable
fun TopicShapeColorPicker(
    nameState: TextFieldState,
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
    val colorScheme = colorScheme
    val motionScheme = motionScheme

    val currentNameTaken by rememberUpdatedState(nameTaken)
    val currentOnNameChange by rememberUpdatedState(onNameChange)

    LaunchedEffect(nameState) {
        snapshotFlow { nameState.text.toString().trim() }
            .drop(1) // The initial name needs no saving
            .debounce(500.milliseconds)
            .collect { name ->
                if (name.isNotEmpty() && !currentNameTaken) currentOnNameChange(name)
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
        TextField(
            state = nameState,
            lineLimits = TextFieldLineLimits.SingleLine,
            isError = nameTaken,
            supportingText = if (nameTaken) {
                { Text(stringResource(Res.string.topic_name_taken)) }
            } else null,
            shape = shapes.large,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
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
            topicColors,
            isPlus = true,
            onColorChange = onColorChange,
            backgroundColor = containerColor,
            horizontalPadding = horizontalPadding,
            dropLast = false
        )

        Text(
            "Shape",
            style = typography.labelLarge,
            color = colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = horizontalPadding, top = 8.dp)
        )
        @OptIn(ExperimentalGridApi::class)
        Grid(
            config = {
                repeat(shapeGridColumns) { column(1.fr) }
                repeat(shapeGridRows) { row(GridTrackSize.Auto) }
                gap(2.dp)
            },
            modifier = Modifier.padding(horizontal = horizontalPadding)
        ) {
            TopicShape.entries.fastForEachIndexed { index, topicShape ->
                val checked = shape == topicShape
                val interactionSource = remember { MutableInteractionSource() }
                val pressed by interactionSource.collectIsPressedAsState()
                val styleState = rememberUpdatedStyleState(null) {
                    it.isChecked = checked
                }
                val rotation = animateFloatAsState(
                    (if (checked) 360f else 0f) + (if (pressed) 90f else 0f),
                    animationSpec = if (pressed) motionScheme.fastSpatialSpec()
                    else motionScheme.slowSpatialSpec()
                )
                val strokeColor = animateColorAsState(
                    if (checked) colorScheme.onPrimaryContainer else colorScheme.primary,
                    animationSpec = motionScheme.defaultEffectsSpec()
                )
                val cellContentShape = topicShape.toShape()

                // Corner rounding logic
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
                val pressedCellShape = RoundedCornerShape(24.dp)

                // A checked cell stays circular while it is pressed
                val targetCellShape = when {
                    checked -> CircleShape
                    pressed -> pressedCellShape
                    else -> cellShape
                }
                val cellShapeAnimated = remember { AnimatedCornerShape(targetCellShape) }
                LaunchedEffect(targetCellShape) {
                    cellShapeAnimated.animateTo(
                        targetCellShape,
                        if (pressed) motionScheme.fastSpatialSpec()
                        else motionScheme.slowSpatialSpec()
                    )
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(cellShapeAnimated)
                        .styleable(styleState) {
                            background(colorScheme.surfaceBright)
                            checked {
                                animate(motionScheme.slowEffectsSpec()) {
                                    background(colorScheme.primaryContainer)
                                }
                            }
                        }
                        .clickable(
                            interactionSource = interactionSource,
                            indication = LocalIndication.current
                        ) {
                            onShapeChange(topicShape)
                        }
                ) {
                    Box(
                        Modifier
                            .padding(16.dp)
                            .fillMaxSize()
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
                                    translate(strokeWidth / 2f, strokeWidth / 2f) {
                                        drawOutline(outline, containerColor)
                                        drawOutline(
                                            outline,
                                            strokeColor.value,
                                            style = stroke
                                        )
                                    }
                                }
                            }
                    )
                }
            }
        }
    }
}
