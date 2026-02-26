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

package org.nsh07.pomodoro.ui.statsScreen.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.AutoScrollCondition
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.Scroll
import com.patrykandpatrick.vico.compose.cartesian.VicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.VicoZoomState
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.decoration.HorizontalLine
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.LineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberFadingEdges
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.DashedShape
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.Position
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.component.ShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import org.nsh07.pomodoro.ui.theme.TomatoTheme
import org.nsh07.pomodoro.utils.millisecondsToHours
import org.nsh07.pomodoro.utils.millisecondsToHoursMinutes
import org.nsh07.pomodoro.utils.millisecondsToMinutes

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TimeLineChart(
    modelProducer: CartesianChartModelProducer,
    goal: Long,
    hoursFormat: String,
    hoursMinutesFormat: String,
    minutesFormat: String,
    modifier: Modifier = Modifier,
    thickness: Dp = 2.dp,
    pointSpacing: Dp = 12.dp,
    xValueFormatter: CartesianValueFormatter = remember { CartesianValueFormatter.decimal() },
    yValueFormatter: CartesianValueFormatter = CartesianValueFormatter { _, value, _ ->
        if (value >= 60 * 60 * 1000) {
            millisecondsToHours(value.toLong(), hoursFormat)
        } else {
            millisecondsToMinutes(value.toLong(), minutesFormat)
        }
    },
    markerValueFormatter: DefaultCartesianMarker.ValueFormatter = DefaultCartesianMarker.ValueFormatter { _, targets ->
        val first = targets.firstOrNull()
        val value = if (first is LineCartesianLayerMarkerTarget) {
            first.points.sumOf { it.entry.y.toLong() }
        } else 0L

        if (value >= 60 * 60 * 1000) {
            millisecondsToHoursMinutes(value, hoursMinutesFormat)
        } else {
            millisecondsToMinutes(value, minutesFormat)
        }
    },
    zoomState: VicoZoomState = rememberVicoZoomState(
        zoomEnabled = true,
        initialZoom = Zoom.fixed(),
        minZoom = Zoom.min(Zoom.Content, Zoom.fixed())
    ),
    scrollState: VicoScrollState = rememberVicoScrollState(
        scrollEnabled = true,
        initialScroll = Scroll.Absolute.End,
        autoScrollCondition = AutoScrollCondition.OnModelGrowth,
        autoScrollAnimationSpec = motionScheme.defaultSpatialSpec()
    ),
    animationSpec: AnimationSpec<Float>? = null
) {
    ProvideVicoTheme(rememberM3VicoTheme()) {
        CartesianChartHost(
            chart =
                rememberCartesianChart(
                    rememberLineCartesianLayer(
                        LineCartesianLayer.LineProvider.series(
                            LineCartesianLayer.rememberLine(
                                fill = LineCartesianLayer.LineFill.double(
                                    topFill = Fill(colorScheme.primary),
                                    bottomFill = Fill(colorScheme.secondary),
                                    splitY = { goal }
                                ),
                                stroke = LineCartesianLayer.LineStroke.Continuous(
                                    thickness, StrokeCap.Round
                                ),
                                areaFill = LineCartesianLayer.AreaFill.double(
                                    topFill = Fill(
                                        verticalGradient(
                                            listOf(
                                                colorScheme.primary,
                                                Color.Transparent
                                            )
                                        )
                                    ),
                                    bottomFill = Fill(
                                        verticalGradient(
                                            listOf(
                                                Color.Transparent,
                                                colorScheme.secondary
                                            )
                                        )
                                    ),
                                    splitY = { goal }
                                ),
                                pointConnector = LineCartesianLayer.PointConnector.cubic(0.5f)
                            )
                        ),
                        pointSpacing = pointSpacing
                    ),
                    startAxis = VerticalAxis.rememberStart(
                        line = rememberLineComponent(Fill.Transparent, 8.dp),
                        label = rememberTextComponent(typography.bodySmall.copy(colorScheme.onSurface)),
                        tick = null,
                        guideline = null,
                        itemPlacer = VerticalAxis.ItemPlacer.count({ 4 }),
                        valueFormatter = yValueFormatter
                    ),
                    bottomAxis = HorizontalAxis.rememberBottom(
                        line = rememberLineComponent(Fill.Transparent, 8.dp),
                        label = rememberTextComponent(typography.bodySmall.copy(colorScheme.onSurface)),
                        tick = null,
                        guideline = null,
                        valueFormatter = xValueFormatter
                    ),
                    decorations = if (goal > 0) listOf(
                        HorizontalLine(
                            y = { goal.toDouble() },
                            line = rememberLineComponent(
                                fill = Fill(colorScheme.primary),
                                thickness = 1.dp,
                                shape = DashedShape(
                                    shape = CircleShape,
                                    dashLength = 2.dp,
                                    gapLength = 2.dp
                                )
                            ),
                            horizontalLabelPosition = Position.Horizontal.Start,
                            verticalLabelPosition = Position.Vertical.Center
                        )
                    )
                    else emptyList(),
                    marker = rememberDefaultCartesianMarker(
                        rememberTextComponent(
                            TextStyle(
                                fontFamily = typography.bodyLarge.fontFamily,
                                color = colorScheme.inverseOnSurface,
                                fontSize = typography.bodySmall.fontSize,
                                lineHeight = typography.bodySmall.lineHeight
                            ),
                            background = rememberShapeComponent(
                                fill = Fill(colorScheme.inverseSurface),
                                shape = shapes.small
                            ),
                            padding = Insets(vertical = 4.dp, horizontal = 8.dp),
                            margins = Insets(bottom = 2.dp)
                        ),
                        valueFormatter = markerValueFormatter,
                        indicator = {
                            ShapeComponent(
                                fill = Fill(it),
                                shape = CircleShape,
                                margins = Insets(3.dp)
                            )
                        },
                        guideline = rememberLineComponent(
                            fill = Fill(colorScheme.primary),
                            shape = DashedShape(
                                shape = CircleShape,
                                dashLength = 2.dp,
                                gapLength = 2.dp
                            )
                        )
                    ),
                    fadingEdges = rememberFadingEdges()
                ),
            modelProducer = modelProducer,
            zoomState = zoomState,
            scrollState = scrollState,
            animationSpec = animationSpec,
            animateIn = false,
            modifier = modifier.height(224.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview
@Composable
private fun TimeLineChartPreview() {
    val modelProducer = remember { CartesianChartModelProducer() }
    val values = mutableListOf<Int>()
    LaunchedEffect(Unit) {
        repeat(365) {
            values.add((0..120).random() * 60 * 1000)
        }
        modelProducer.runTransaction {
            lineSeries {
                series(values)
            }
        }
    }
    TomatoTheme {
        Surface {
            TimeLineChart(
                modelProducer = modelProducer,
                goal = 60 * 60 * 1000,
                hoursFormat = "%dh",
                hoursMinutesFormat = "%dh %dm",
                minutesFormat = "%dm"
            )
        }
    }
}
