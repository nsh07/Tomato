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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.nsh07.pomodoro.data.Topic
import org.nsh07.pomodoro.data.TopicShape
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsAction
import org.nsh07.pomodoro.ui.theme.SeededTheme
import org.nsh07.pomodoro.ui.theme.TomatoTheme

private const val shapeGridColumns = 5
private val shapeGridRows =
    (TopicShape.entries.size + shapeGridColumns - 1) / shapeGridColumns

private val topicColors = listOf(
    Color(0xfffeb4a7), Color(0xffffb3c0), Color(0xfffcaaff), Color(0xffb9c3ff),
    Color(0xff62d3ff), Color(0xff44d9f1), Color(0xff52dbc9), Color(0xff78dd77),
    Color(0xff9fd75c), Color(0xffc1d02d), Color(0xfffabd00), Color(0xffffb86e)
)

/**
 * A [Shape] whose corners animate towards those of the shape passed to [animateTo].
 *
 * A shape set through [styleable] is only used for drawing: its clip is applied by a graphics layer
 * that is refreshed when a *layer* property changes, and a shape change alone does not refresh it.
 * A cell would therefore keep clipping its ripple to the shape it was first laid out with. Clipping
 * with this shape instead keeps the cell background, its ripple and its contents in sync while the
 * shape morphs, since the corner animations are read while the layer resolves its outline.
 */
@Stable
private class AnimatedCornerShape(initialShape: RoundedCornerShape) : Shape {
    private var shape = initialShape
    private var size = Size.Zero
    private var density = Density(1f)
    private var corners: Array<Animatable<Float, AnimationVector1D>>? = null

    /**
     * Animates the corners towards those of [target]. Corners of a shape that has not been laid out
     * yet snap instead, as corner sizes cannot be resolved without knowing the size of the shape.
     */
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
                    .padding(bottom = 16.dp)
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
                        val shape = topicShape.toShape()

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
                                    onAction(SettingsAction.SetEditingTopicShape(topicShape))
                                }
                        ) {
                            Box(
                                Modifier
                                    .padding(16.dp)
                                    .fillMaxSize()
                                    .graphicsLayer { rotationZ = rotation.value }
                                    .drawWithCache { // shape
                                        val strokeWidth = 2.dp.toPx()
                                        val outline = shape.createOutline(
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
                                                drawOutline(outline, colorScheme.surfaceContainer)
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
                setShowSheet = {},
                onAction = { action ->
                    when (action) {
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
