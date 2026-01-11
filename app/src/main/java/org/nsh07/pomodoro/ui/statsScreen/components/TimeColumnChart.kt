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

import android.graphics.Typeface
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberFadingEdges
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.decoration.HorizontalLine
import com.patrykandpatrick.vico.core.cartesian.marker.ColumnCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.Insets
import com.patrykandpatrick.vico.core.common.Position
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import com.patrykandpatrick.vico.core.common.shape.DashedShape
import org.nsh07.pomodoro.ui.theme.TomatoTheme
import org.nsh07.pomodoro.utils.millisecondsToHours
import org.nsh07.pomodoro.utils.millisecondsToHoursMinutes
import org.nsh07.pomodoro.utils.millisecondsToMinutes

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TimeColumnChart(
    modelProducer: CartesianChartModelProducer,
    goal: Long = 4 * 60 * 60 * 1000,
    hoursFormat: String,
    hoursMinutesFormat: String,
    minutesFormat: String,
    modifier: Modifier = Modifier,
    axisTypeface: Typeface = Typeface.DEFAULT,
    markerTypeface: Typeface = Typeface.DEFAULT,
    thickness: Dp = 40.dp,
    columnCollectionSpacing: Dp = 4.dp,
    xValueFormatter: CartesianValueFormatter = CartesianValueFormatter.Default,
    yValueFormatter: CartesianValueFormatter = CartesianValueFormatter { _, value, _ ->
        if (value >= 60 * 60 * 1000) {
            millisecondsToHours(value.toLong(), hoursFormat)
        } else {
            millisecondsToMinutes(value.toLong(), minutesFormat)
        }
    },
    markerValueFormatter: DefaultCartesianMarker.ValueFormatter = DefaultCartesianMarker.ValueFormatter { _, targets ->
        val first = targets.firstOrNull()
        val value = if (first is ColumnCartesianLayerMarkerTarget) {
            first.columns.sumOf { it.entry.y.toLong() }
        } else 0L

        if (value >= 60 * 60 * 1000) {
            millisecondsToHoursMinutes(value, hoursMinutesFormat)
        } else {
            millisecondsToMinutes(value, minutesFormat)
        }
    },
    animationSpec: AnimationSpec<Float>? = motionScheme.defaultEffectsSpec()
) {
    ProvideVicoTheme(rememberM3VicoTheme()) {
        CartesianChartHost(
            chart =
                rememberCartesianChart(
                    rememberColumnCartesianLayer(
                        Providers.columnProviderWithLimit(
                            limit = goal,
                            belowLimitComponent = rememberLineComponent(
                                fill = fill(colorScheme.primary.copy(0.5f)),
                                thickness = thickness,
                                shape = CorneredShape.Pill
                            ),
                            aboveLimitComponent = rememberLineComponent(
                                fill = fill(colorScheme.primary),
                                thickness = thickness,
                                shape = CorneredShape.Pill
                            )
                        ),
                        columnCollectionSpacing = columnCollectionSpacing
                    ),
                    startAxis = VerticalAxis.rememberStart(
                        line = null,
                        label = rememberTextComponent(colorScheme.onSurface, axisTypeface),
                        tick = null,
                        guideline = null,
                        itemPlacer = VerticalAxis.ItemPlacer.count({ 4 }),
                        valueFormatter = yValueFormatter
                    ),
                    bottomAxis = HorizontalAxis.rememberBottom(
                        line = null,
                        label = rememberTextComponent(colorScheme.onSurface, axisTypeface),
                        tick = null,
                        guideline = null,
                        valueFormatter = xValueFormatter
                    ),
                    decorations = listOf(
                        HorizontalLine(
                            y = { goal.toDouble() },
                            line = rememberLineComponent(
                                fill = fill(colorScheme.primary),
                                thickness = 1.dp,
                                shape = DashedShape(
                                    shape = CorneredShape.Pill,
                                    dashLengthDp = 16f,
                                    gapLengthDp = 8f
                                )
                            ),
                            horizontalLabelPosition = Position.Horizontal.Start,
                            verticalLabelPosition = Position.Vertical.Center
                        )
                    ),
                    marker = rememberDefaultCartesianMarker(
                        rememberTextComponent(
                            color = colorScheme.inverseOnSurface,
                            typeface = markerTypeface,
                            background = rememberShapeComponent(
                                fill = fill(colorScheme.inverseSurface),
                                shape = CorneredShape.rounded(8f)
                            ),
                            textSize = typography.bodySmall.fontSize,
                            lineHeight = typography.bodySmall.lineHeight,
                            padding = Insets(verticalDp = 4f, horizontalDp = 8f),
                            margins = Insets(bottomDp = 2f)
                        ),
                        valueFormatter = markerValueFormatter,
                        guideline = rememberLineComponent(
                            fill = fill(colorScheme.primary),
                            shape = DashedShape(
                                shape = CorneredShape.Pill,
                                dashLengthDp = 16f,
                                gapLengthDp = 8f
                            )
                        )
                    ),
                    fadingEdges = rememberFadingEdges()
                ),
            modelProducer = modelProducer,
            zoomState = rememberVicoZoomState(
                zoomEnabled = false,
                initialZoom = Zoom.fixed(),
                minZoom = Zoom.min(Zoom.Content, Zoom.fixed())
            ),
            animationSpec = animationSpec,
            animateIn = false,
            modifier = modifier.height(226.dp),
        )
    }
}

@Preview
@Composable
private fun TimeColumnChartPreview() {
    val modelProducer = remember { CartesianChartModelProducer() }
    val values = mutableListOf<Int>()
    LaunchedEffect(Unit) {
        repeat(30) {
            values.add((0..120).random() * 60 * 1000)
        }
        modelProducer.runTransaction {
            columnSeries {
                series(values)
            }
        }
    }
    TomatoTheme {
        Surface {
            TimeColumnChart(
                thickness = 8.dp,
                modelProducer = modelProducer,
                goal = 60 * 60 * 1000L,
                hoursFormat = "%dh",
                hoursMinutesFormat = "%dh %dm",
                minutesFormat = "%dm"
            )
        }
    }
}
