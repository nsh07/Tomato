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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.ui.mergePaddingValues
import org.nsh07.pomodoro.ui.statsScreen.components.FocusBreakRatioVisualization
import org.nsh07.pomodoro.ui.statsScreen.components.TimeColumnChart
import org.nsh07.pomodoro.ui.statsScreen.components.VariableWidth1DHeatmap
import org.nsh07.pomodoro.ui.statsScreen.components.sharedBoundsReveal
import org.nsh07.pomodoro.ui.theme.AppFonts.robotoFlexTopBar
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.topListItemShape
import org.nsh07.pomodoro.utils.millisecondsToHoursMinutes
import org.nsh07.pomodoro.utils.millisecondsToMinutes

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SharedTransitionScope.LastWeekScreen(
    contentPadding: PaddingValues,
    lastWeekAnalysisValues: Pair<List<Long>, Long>,
    lastWeekSummaryValues: List<Pair<String, List<Long>>>,
    lastWeekSummaryChartData: Pair<CartesianChartModelProducer, ExtraStore.Key<List<String>>>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    hoursMinutesFormat: String,
    hoursFormat: String,
    minutesFormat: String,
    axisTypeface: Typeface,
    markerTypeface: Typeface
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val rankList = remember(lastWeekAnalysisValues) {
        val sortedIndices =
            lastWeekAnalysisValues.first.indices.sortedByDescending { lastWeekAnalysisValues.first[it] }
        val ranks = MutableList(lastWeekAnalysisValues.first.size) { 0 }

        sortedIndices.forEachIndexed { rank, originalIndex ->
            ranks[originalIndex] = rank
        }

        ranks
    }

    val focusDuration = remember(lastWeekAnalysisValues) {
        lastWeekAnalysisValues.first.sum()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.last_week),
                        fontFamily = robotoFlexTopBar,
                        modifier = Modifier.sharedBounds(
                            sharedContentState = this@LastWeekScreen
                                .rememberSharedContentState("last week heading"),
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
                sharedTransitionScope = this@LastWeekScreen,
                sharedContentState = this@LastWeekScreen.rememberSharedContentState(
                    "last week card"
                ),
                animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                clipShape = topListItemShape
            )
    ) { innerPadding ->
        val insets = mergePaddingValues(innerPadding, contentPadding)
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = insets,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        millisecondsToHoursMinutes(
                            focusDuration,
                            hoursMinutesFormat
                        ),
                        style = typography.displaySmall,
                        modifier = Modifier
                            .sharedElement(
                                sharedContentState = this@LastWeekScreen
                                    .rememberSharedContentState("last week average focus timer"),
                                animatedVisibilityScope = LocalNavAnimatedContentScope.current
                            )
                    )
                    Text(
                        stringResource(R.string.focus_per_day_avg),
                        style = typography.titleSmall,
                        modifier = Modifier
                            .padding(bottom = 5.2.dp)
                            .sharedElement(
                                sharedContentState = this@LastWeekScreen
                                    .rememberSharedContentState("focus per day average (week)"),
                                animatedVisibilityScope = LocalNavAnimatedContentScope.current
                            )
                    )
                }
            }
            item {
                TimeColumnChart(
                    modelProducer = lastWeekSummaryChartData.first,
                    hoursFormat = hoursFormat,
                    hoursMinutesFormat = hoursMinutesFormat,
                    minutesFormat = minutesFormat,
                    axisTypeface = axisTypeface,
                    markerTypeface = markerTypeface,
                    xValueFormatter = CartesianValueFormatter { context, x, _ ->
                        context.model.extraStore[lastWeekSummaryChartData.second][x.toInt()]
                    },
                    modifier = Modifier
                        .sharedElement(
                            sharedContentState = this@LastWeekScreen
                                .rememberSharedContentState("last week chart"),
                            animatedVisibilityScope = LocalNavAnimatedContentScope.current
                        )
                )
            }

            item { Spacer(Modifier.height(8.dp)) }

            item {
                Text(
                    stringResource(R.string.focus_overview),
                    style = typography.headlineSmall
                )
                Text(
                    stringResource(R.string.focus_overview_desc),
                    style = typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
            }
            item { VariableWidth1DHeatmap(lastWeekAnalysisValues.first, rankList = rankList) }
            item {
                Row {
                    lastWeekAnalysisValues.first.fastForEach {
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

            item { Spacer(Modifier.height(8.dp)) }

            item {
                Text(
                    stringResource(R.string.focus_break_ratio),
                    style = typography.headlineSmall
                )
            }
            item {
                FocusBreakRatioVisualization(
                    focusDuration = focusDuration,
                    breakDuration = lastWeekAnalysisValues.second
                )
            }

            item { Spacer(Modifier.height(8.dp)) }

            item {
                Text(
                    stringResource(R.string.focus_insights),
                    style = typography.headlineSmall
                )
                Text(
                    stringResource(R.string.focus_insights_desc),
                    style = typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    lastWeekSummaryValues.fastForEach {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                it.first,
                                style = typography.labelSmall,
                                modifier = Modifier.size(18.dp)
                            )
                            VariableWidth1DHeatmap(it.second, rankList = rankList)
                        }
                    }
                }
            }
        }
    }
}