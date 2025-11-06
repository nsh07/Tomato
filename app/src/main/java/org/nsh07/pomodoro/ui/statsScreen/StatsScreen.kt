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

package org.nsh07.pomodoro.ui.statsScreen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import org.nsh07.pomodoro.BuildConfig
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.data.Stat
import org.nsh07.pomodoro.ui.statsScreen.viewModel.StatsViewModel
import org.nsh07.pomodoro.ui.theme.AppFonts.interClock
import org.nsh07.pomodoro.ui.theme.AppFonts.robotoFlexTopBar
import org.nsh07.pomodoro.ui.theme.TomatoTheme
import org.nsh07.pomodoro.utils.millisecondsToHoursMinutes

@Composable
fun StatsScreenRoot(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = viewModel(factory = StatsViewModel.Factory)
) {
    val todayStat by viewModel.todayStat.collectAsStateWithLifecycle(null)

    val lastWeekSummaryChartData by viewModel.lastWeekSummaryChartData.collectAsStateWithLifecycle()
    val lastWeekAnalysisValues by viewModel.lastWeekAverageFocusTimes.collectAsStateWithLifecycle()

    val lastMonthSummaryChartData by viewModel.lastMonthSummaryChartData.collectAsStateWithLifecycle()
    val lastMonthAnalysisValues by viewModel.lastMonthAverageFocusTimes.collectAsStateWithLifecycle()

    val lastYearSummaryChartData by viewModel.lastYearSummaryChartData.collectAsStateWithLifecycle()
    val lastYearAnalysisValues by viewModel.lastYearAverageFocusTimes.collectAsStateWithLifecycle()

    StatsScreen(
        contentPadding = contentPadding,
        lastWeekSummaryChartData = lastWeekSummaryChartData,
        lastMonthSummaryChartData = lastMonthSummaryChartData,
        lastYearSummaryChartData = lastYearSummaryChartData,
        todayStat = todayStat,
        lastWeekAverageFocusTimes = lastWeekAnalysisValues,
        lastMonthAverageFocusTimes = lastMonthAnalysisValues,
        lastYearAverageFocusTimes = lastYearAnalysisValues,
        generateSampleData = viewModel::generateSampleData,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StatsScreen(
    contentPadding: PaddingValues,
    lastWeekSummaryChartData: Pair<CartesianChartModelProducer, ExtraStore.Key<List<String>>>,
    lastMonthSummaryChartData: Pair<CartesianChartModelProducer, ExtraStore.Key<List<String>>>,
    lastYearSummaryChartData: Pair<CartesianChartModelProducer, ExtraStore.Key<List<String>>>,
    todayStat: Stat?,
    lastWeekAverageFocusTimes: List<Int>,
    lastMonthAverageFocusTimes: List<Int>,
    lastYearAverageFocusTimes: List<Int>,
    generateSampleData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    var lastWeekStatExpanded by rememberSaveable { mutableStateOf(false) }
    var lastMonthStatExpanded by rememberSaveable { mutableStateOf(false) }

    val lastWeekSummaryAnalysisModelProducer = remember { CartesianChartModelProducer() }
    val lastMonthSummaryAnalysisModelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(lastWeekAverageFocusTimes) {
        lastWeekSummaryAnalysisModelProducer.runTransaction {
            columnSeries {
                series(lastWeekAverageFocusTimes)
            }
        }
    }

    LaunchedEffect(lastMonthAverageFocusTimes) {
        lastMonthSummaryAnalysisModelProducer.runTransaction {
            columnSeries {
                series(lastMonthAverageFocusTimes)
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        TopAppBar(
            title = {
                Text(
                    stringResource(R.string.stats),
                    style = LocalTextStyle.current.copy(
                        fontFamily = robotoFlexTopBar,
                        fontSize = 32.sp,
                        lineHeight = 32.sp
                    ),
                    modifier = Modifier
                        .padding(top = contentPadding.calculateTopPadding())
                        .padding(vertical = 14.dp)
                )
            },
            actions = if (BuildConfig.DEBUG) {
                {
                    IconButton(
                        onClick = generateSampleData
                    ) {
                        Spacer(Modifier.size(24.dp))
                    }
                }
            } else {
                {}
            },
            subtitle = {},
            titleHorizontalAlignment = Alignment.CenterHorizontally,
            scrollBehavior = scrollBehavior,
            windowInsets = WindowInsets()
        )

        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier) }
            item {
                Text(
                    stringResource(R.string.today),
                    style = typography.headlineSmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }
            item {
                Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Box(
                        modifier = Modifier
                            .background(
                                colorScheme.primaryContainer,
                                MaterialTheme.shapes.largeIncreased
                            )
                            .weight(1f)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                stringResource(R.string.focus),
                                style = typography.titleMedium,
                                color = colorScheme.onPrimaryContainer
                            )
                            Text(
                                remember(todayStat) {
                                    millisecondsToHoursMinutes(todayStat?.totalFocusTime() ?: 0)
                                },
                                style = typography.displaySmall,
                                fontFamily = interClock,
                                color = colorScheme.onPrimaryContainer,
                                maxLines = 1,
                                autoSize = TextAutoSize.StepBased(maxFontSize = typography.displaySmall.fontSize)
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                colorScheme.tertiaryContainer,
                                MaterialTheme.shapes.largeIncreased
                            )
                            .weight(1f)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                stringResource(R.string.break_),
                                style = typography.titleMedium,
                                color = colorScheme.onTertiaryContainer
                            )
                            Text(
                                remember(todayStat) {
                                    millisecondsToHoursMinutes(todayStat?.breakTime ?: 0)
                                },
                                style = typography.displaySmall,
                                fontFamily = interClock,
                                color = colorScheme.onTertiaryContainer,
                                maxLines = 1,
                                autoSize = TextAutoSize.StepBased(maxFontSize = typography.displaySmall.fontSize)
                            )
                        }
                    }
                }
            }
            item { Spacer(Modifier) }
            item {
                Text(
                    stringResource(R.string.last_week),
                    style = typography.headlineSmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }
            item {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        millisecondsToHoursMinutes(
                            remember(lastWeekAverageFocusTimes) {
                                lastWeekAverageFocusTimes.sum().toLong()
                            }
                        ),
                        style = typography.displaySmall,
                        fontFamily = interClock
                    )
                    Text(
                        stringResource(R.string.focus_per_day_avg),
                        style = typography.titleSmall,
                        modifier = Modifier.padding(bottom = 6.3.dp)
                    )
                }
            }
            item {
                TimeColumnChart(
                    lastWeekSummaryChartData.first,
                    modifier = Modifier.padding(start = 16.dp),
                    xValueFormatter = CartesianValueFormatter { context, x, _ ->
                        context.model.extraStore[lastWeekSummaryChartData.second][x.toInt()]
                    }
                )
            }
            item {
                val iconRotation by animateFloatAsState(
                    if (lastWeekStatExpanded) 180f else 0f,
                    animationSpec = motionScheme.defaultSpatialSpec()
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(Modifier.height(2.dp))
                    FilledTonalIconToggleButton(
                        checked = lastWeekStatExpanded,
                        onCheckedChange = { lastWeekStatExpanded = it },
                        shapes = IconButtonDefaults.toggleableShapes(),
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .width(52.dp)
                            .align(Alignment.End)
                    ) {
                        Icon(
                            painterResource(R.drawable.arrow_down),
                            stringResource(R.string.more_info),
                            modifier = Modifier.rotate(iconRotation)
                        )
                    }
                    ProductivityGraph(
                        lastWeekStatExpanded,
                        lastWeekSummaryAnalysisModelProducer,
                        label = stringResource(R.string.weekly_productivity_analysis),
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
            item { Spacer(Modifier) }
            item {
                Text(
                    stringResource(R.string.last_month),
                    style = typography.headlineSmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }
            item {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        millisecondsToHoursMinutes(
                            remember(lastMonthAverageFocusTimes) {
                                lastMonthAverageFocusTimes.sum().toLong()
                            }
                        ),
                        style = typography.displaySmall,
                        fontFamily = interClock
                    )
                    Text(
                        text = stringResource(R.string.focus_per_day_avg),
                        style = typography.titleSmall,
                        modifier = Modifier.padding(bottom = 6.3.dp)
                    )
                }
            }
            item {
                TimeColumnChart(
                    lastMonthSummaryChartData.first,
                    modifier = Modifier.padding(start = 16.dp),
                    thickness = 8.dp,
                    xValueFormatter = CartesianValueFormatter { context, x, _ ->
                        context.model.extraStore[lastMonthSummaryChartData.second][x.toInt()]
                    }
                )
            }
            item {
                val iconRotation by animateFloatAsState(
                    if (lastMonthStatExpanded) 180f else 0f,
                    animationSpec = motionScheme.defaultSpatialSpec()
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(Modifier.height(2.dp))
                    FilledTonalIconToggleButton(
                        checked = lastMonthStatExpanded,
                        onCheckedChange = { lastMonthStatExpanded = it },
                        shapes = IconButtonDefaults.toggleableShapes(),
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .width(52.dp)
                            .align(Alignment.End)
                    ) {
                        Icon(
                            painterResource(R.drawable.arrow_down),
                            stringResource(R.string.more_info),
                            modifier = Modifier.rotate(iconRotation)
                        )
                    }
                    ProductivityGraph(
                        lastMonthStatExpanded,
                        lastMonthSummaryAnalysisModelProducer,
                        label = stringResource(R.string.monthly_productivity_analysis),
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
            item { Spacer(Modifier) }
            item {
                Text(
                    stringResource(R.string.last_year),
                    style = typography.headlineSmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }
            item {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        millisecondsToHoursMinutes(
                            remember(lastYearAverageFocusTimes) {
                                lastYearAverageFocusTimes.sum().toLong()
                            }
                        ),
                        style = typography.displaySmall,
                        fontFamily = interClock
                    )
                    Text(
                        text = stringResource(R.string.focus_per_day_avg),
                        style = typography.titleSmall,
                        modifier = Modifier.padding(bottom = 6.3.dp)
                    )
                }
            }
            item {
                TimeLineChart(
                    lastYearSummaryChartData.first,
                    modifier = Modifier.padding(start = 16.dp),
                    xValueFormatter = CartesianValueFormatter { context, x, _ ->
                        context.model.extraStore[lastYearSummaryChartData.second][x.toInt()]
                    }
                )
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Preview(
    widthDp = 400
)
@Composable
fun StatsScreenPreview() {
    val modelProducer = remember { CartesianChartModelProducer() }
    val keys = remember { ExtraStore.Key<List<String>>() }

    LaunchedEffect(Unit) {
        modelProducer.runTransaction {
            columnSeries {
                series(5, 6, 5, 2, 11, 8, 5)
            }
            lineSeries {}
            extras { it[keys] = listOf("M", "T", "W", "T", "F", "S", "S") }
        }
    }

    TomatoTheme {
        Surface {
            StatsScreen(
                PaddingValues(),
                Pair(modelProducer, keys),
                Pair(modelProducer, keys),
                Pair(modelProducer, keys),
                null,
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
                {}
            )
        }
    }
}