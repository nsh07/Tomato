/*
 * Copyright (c) 2025 Nishant Mishra
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

package org.nsh07.pomodoro.ui.statsScreen.screens

import android.graphics.Typeface
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TonalToggleButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.ui.mergePaddingValues
import org.nsh07.pomodoro.ui.statsScreen.components.FocusBreakRatioVisualization
import org.nsh07.pomodoro.ui.statsScreen.components.FocusBreakdownChart
import org.nsh07.pomodoro.ui.statsScreen.components.HEATMAP_CELL_GAP
import org.nsh07.pomodoro.ui.statsScreen.components.HEATMAP_CELL_SIZE
import org.nsh07.pomodoro.ui.statsScreen.components.HeatmapWithWeekLabels
import org.nsh07.pomodoro.ui.statsScreen.components.HorizontalStackedBar
import org.nsh07.pomodoro.ui.statsScreen.components.TimeLineChart
import org.nsh07.pomodoro.ui.statsScreen.components.sharedBoundsReveal
import org.nsh07.pomodoro.ui.theme.AppFonts.robotoFlexTopBar
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.bottomListItemShape
import org.nsh07.pomodoro.utils.millisecondsToHoursMinutes
import org.nsh07.pomodoro.utils.millisecondsToMinutes

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SharedTransitionScope.LastYearScreen(
    contentPadding: PaddingValues,
    focusBreakdownValues: Pair<List<Long>, Long>,
    focusHeatmapData: List<List<Long>?>,
    heatmapMaxValue: Long,
    mainChartData: Pair<CartesianChartModelProducer, ExtraStore.Key<List<String>>>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    hoursMinutesFormat: String,
    hoursFormat: String,
    minutesFormat: String,
    axisTypeface: Typeface,
    markerTypeface: Typeface
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val lastYearSummaryAnalysisModelProducer = remember { CartesianChartModelProducer() }
    var breakdownChartExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(focusBreakdownValues.first) {
        lastYearSummaryAnalysisModelProducer.runTransaction {
            columnSeries {
                series(focusBreakdownValues.first)
            }
        }
    }

    val rankList = remember(focusBreakdownValues) {
        val sortedIndices =
            focusBreakdownValues.first.indices.sortedByDescending { focusBreakdownValues.first[it] }
        val ranks = MutableList(focusBreakdownValues.first.size) { 0 }

        sortedIndices.forEachIndexed { rank, originalIndex ->
            ranks[originalIndex] = rank
        }

        ranks
    }

    val focusDuration = remember(focusBreakdownValues) {
        focusBreakdownValues.first.sum()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.last_year),
                        fontFamily = robotoFlexTopBar,
                        modifier = Modifier.sharedBounds(
                            sharedContentState = this@LastYearScreen
                                .rememberSharedContentState("last year heading"),
                            animatedVisibilityScope = LocalNavAnimatedContentScope.current
                        )
                    )
                },
                subtitle = {
                    Text(stringResource(R.string.stats))
                },
                navigationIcon = {
                    FilledTonalIconButton(
                        onClick = onBack,
                        shapes = IconButtonDefaults.shapes()
                    ) {
                        Icon(
                            painterResource(R.drawable.arrow_back),
                            null
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .sharedBoundsReveal(
                sharedTransitionScope = this@LastYearScreen,
                sharedContentState = this@LastYearScreen.rememberSharedContentState(
                    "last year card"
                ),
                animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                clipShape = bottomListItemShape
            )
    ) { innerPadding ->
        val insets = mergePaddingValues(innerPadding, contentPadding)
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = insets,
            modifier = Modifier.fillMaxSize() // we don't add padding here to allow charts to extend to the edge
        ) {
            item {
                Spacer(Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        millisecondsToHoursMinutes(
                            focusDuration,
                            hoursMinutesFormat
                        ),
                        style = typography.displaySmall,
                        modifier = Modifier
                            .sharedElement(
                                sharedContentState = this@LastYearScreen
                                    .rememberSharedContentState("last year average focus timer"),
                                animatedVisibilityScope = LocalNavAnimatedContentScope.current
                            )
                    )
                    Text(
                        stringResource(R.string.focus_per_day_avg),
                        style = typography.titleSmall,
                        modifier = Modifier
                            .padding(bottom = 5.2.dp)
                            .sharedElement(
                                sharedContentState = this@LastYearScreen
                                    .rememberSharedContentState("focus per day average (year)"),
                                animatedVisibilityScope = LocalNavAnimatedContentScope.current
                            )
                    )
                }
            }
            item {
                TimeLineChart(
                    modelProducer = mainChartData.first,
                    hoursFormat = hoursFormat,
                    hoursMinutesFormat = hoursMinutesFormat,
                    minutesFormat = minutesFormat,
                    axisTypeface = axisTypeface,
                    markerTypeface = markerTypeface,
                    xValueFormatter = CartesianValueFormatter { context, x, _ ->
                        context.model.extraStore[mainChartData.second][x.toInt()]
                    },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .sharedElement(
                            sharedContentState = this@LastYearScreen
                                .rememberSharedContentState("last year chart"),
                            animatedVisibilityScope = LocalNavAnimatedContentScope.current
                        )
                )
            }

            item { Spacer(Modifier.height(8.dp)) }

            item {
                Text(
                    stringResource(R.string.focus_breakdown),
                    style = typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Text(
                    stringResource(R.string.focus_breakdown_desc),
                    style = typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                HorizontalStackedBar(
                    focusBreakdownValues.first,
                    rankList = rankList,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            item {
                Row(Modifier.padding(horizontal = 16.dp)) {
                    focusBreakdownValues.first.fastForEach {
                        Text(
                            if (it <= 60 * 60 * 1000)
                                millisecondsToMinutes(it, minutesFormat)
                            else millisecondsToHoursMinutes(it, hoursMinutesFormat),
                            style = typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                val iconRotation by animateFloatAsState(
                    if (breakdownChartExpanded) 180f else 0f
                )
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                ) {
                    TonalToggleButton(
                        checked = breakdownChartExpanded,
                        onCheckedChange = { breakdownChartExpanded = it },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(
                            painterResource(R.drawable.arrow_down),
                            stringResource(R.string.more_info),
                            modifier = Modifier.rotate(iconRotation)
                        )
                        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                        Text("Show chart")
                    }

                    FocusBreakdownChart(
                        expanded = breakdownChartExpanded,
                        modelProducer = lastYearSummaryAnalysisModelProducer,
                        modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
                    )
                }
            }

            item {
                Text(
                    stringResource(R.string.focus_break_ratio),
                    style = typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            item {
                FocusBreakRatioVisualization(
                    focusDuration = focusDuration,
                    breakDuration = focusBreakdownValues.second,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item { Spacer(Modifier.height(8.dp)) }

            item {
                Text(
                    "Focus history heatmap",
                    style = typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Text(
                    "Focus history of the past year. Deeper colors represent a longer focus duration. Cells are grouped by month.",
                    style = typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            item {
                HeatmapWithWeekLabels(
                    data = focusHeatmapData,
                    maxValue = heatmapMaxValue,
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            item { // Heatmap guide
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Less",
                        color = colorScheme.onSurfaceVariant,
                        style = typography.labelMedium
                    )
                    Spacer(Modifier.width(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(HEATMAP_CELL_GAP),
                        modifier = Modifier.clip(shapes.small)
                    ) {
                        Spacer(
                            Modifier
                                .size(HEATMAP_CELL_SIZE)
                                .background(colorScheme.surfaceVariant, shapes.extraSmall)
                        )
                        (4..10 step 3).forEach {
                            Spacer(
                                Modifier
                                    .size(HEATMAP_CELL_SIZE)
                                    .background(
                                        colorScheme.primary.copy(it.toFloat() / 10f),
                                        shapes.extraSmall
                                    )
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "More",
                        color = colorScheme.onSurfaceVariant,
                        style = typography.labelMedium
                    )
                }
            }
        }
    }
}