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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import org.nsh07.pomodoro.BuildConfig
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.data.Stat
import org.nsh07.pomodoro.ui.Screen
import org.nsh07.pomodoro.ui.mergePaddingValues
import org.nsh07.pomodoro.ui.statsScreen.components.TimeColumnChart
import org.nsh07.pomodoro.ui.statsScreen.components.TimeLineChart
import org.nsh07.pomodoro.ui.statsScreen.components.sharedBoundsReveal
import org.nsh07.pomodoro.ui.theme.AppFonts.robotoFlexTopBar
import org.nsh07.pomodoro.ui.theme.CustomColors.listItemColors
import org.nsh07.pomodoro.ui.theme.CustomColors.topBarColors
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.bottomListItemShape
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.middleListItemShape
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.topListItemShape
import org.nsh07.pomodoro.utils.millisecondsToHoursMinutes

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SharedTransitionScope.StatsMainScreen(
    contentPadding: PaddingValues,
    lastWeekSummaryChartData: Pair<CartesianChartModelProducer, ExtraStore.Key<List<String>>>,
    lastMonthSummaryChartData: Pair<CartesianChartModelProducer, ExtraStore.Key<List<String>>>,
    lastYearSummaryChartData: Pair<CartesianChartModelProducer, ExtraStore.Key<List<String>>>,
    todayStat: Stat?,
    lastWeekAverageFocusTimes: List<Int>,
    lastMonthAverageFocusTimes: List<Int>,
    lastYearAverageFocusTimes: List<Int>,
    generateSampleData: () -> Unit,
    hoursMinutesFormat: String,
    hoursFormat: String,
    minutesFormat: String,
    axisTypeface: Typeface,
    markerTypeface: Typeface,
    onNavigate: (Screen.Stats) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
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
                colors = topBarColors,
                windowInsets = WindowInsets()
            )
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        val insets = mergePaddingValues(innerPadding, contentPadding)
        LazyColumn(
            contentPadding = insets,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                .background(topBarColors.containerColor)
                .padding(horizontal = 16.dp)
        ) {
            item { Spacer(Modifier.height(14.dp)) }

            item {
                Text(
                    stringResource(R.string.today),
                    style = typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item { Spacer(Modifier.height(12.dp)) }

            item {
                Row {
                    Box(
                        modifier = Modifier
                            .background(
                                colorScheme.primaryContainer,
                                shapes.largeIncreased
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
                                    millisecondsToHoursMinutes(
                                        todayStat?.totalFocusTime() ?: 0,
                                        hoursMinutesFormat
                                    )
                                },
                                style = typography.displaySmall,
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
                                shapes.largeIncreased
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
                                    millisecondsToHoursMinutes(
                                        todayStat?.breakTime ?: 0,
                                        hoursMinutesFormat
                                    )
                                },
                                style = typography.displaySmall,
                                color = colorScheme.onTertiaryContainer,
                                maxLines = 1,
                                autoSize = TextAutoSize.StepBased(maxFontSize = typography.displaySmall.fontSize)
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(12.dp)) }

            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .sharedBoundsReveal(
                            sharedTransitionScope = this@StatsMainScreen,
                            sharedContentState = this@StatsMainScreen.rememberSharedContentState(
                                "last week card"
                            ),
                            animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                            clipShape = topListItemShape
                        )
                        .clip(topListItemShape)
                        .background(listItemColors.containerColor)
                        .clickable { onNavigate(Screen.Stats.LastWeek) }
                        .padding(
                            start = 20.dp,
                            top = 20.dp,
                            bottom = 20.dp
                        ) // end = 0 to let the chart touch the end
                ) {
                    Text(
                        stringResource(R.string.last_week),
                        style = typography.headlineSmall,
                        modifier = Modifier.sharedBounds(
                            sharedContentState = this@StatsMainScreen
                                .rememberSharedContentState("last week heading"),
                            animatedVisibilityScope = LocalNavAnimatedContentScope.current
                        )
                    )

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            millisecondsToHoursMinutes(
                                remember(lastWeekAverageFocusTimes) {
                                    lastWeekAverageFocusTimes.sum().toLong()
                                },
                                hoursMinutesFormat
                            ),
                            style = typography.displaySmall,
                            modifier = Modifier
                                .sharedElement(
                                    sharedContentState = this@StatsMainScreen
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
                                    sharedContentState = this@StatsMainScreen
                                        .rememberSharedContentState("focus per day average (week)"),
                                    animatedVisibilityScope = LocalNavAnimatedContentScope.current
                                )
                        )
                    }

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
                                sharedContentState = this@StatsMainScreen
                                    .rememberSharedContentState("last week chart"),
                                animatedVisibilityScope = LocalNavAnimatedContentScope.current
                            )
                    )
                }
            }

            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .clip(middleListItemShape)
                        .background(listItemColors.containerColor)
                        .clickable {}
                        .padding(
                            start = 20.dp,
                            top = 20.dp,
                            bottom = 20.dp
                        ) // end = 0 to let the chart touch the end
                ) {
                    Text(
                        stringResource(R.string.last_month),
                        style = typography.headlineSmall
                    )

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            millisecondsToHoursMinutes(
                                remember(lastMonthAverageFocusTimes) {
                                    lastMonthAverageFocusTimes.sum().toLong()
                                },
                                hoursMinutesFormat
                            ),
                            style = typography.displaySmall
                        )
                        Text(
                            text = stringResource(R.string.focus_per_day_avg),
                            style = typography.titleSmall,
                            modifier = Modifier.padding(bottom = 5.2.dp)
                        )
                    }

                    TimeColumnChart(
                        modelProducer = lastMonthSummaryChartData.first,
                        hoursFormat = hoursFormat,
                        hoursMinutesFormat = hoursMinutesFormat,
                        minutesFormat = minutesFormat,
                        axisTypeface = axisTypeface,
                        markerTypeface = markerTypeface,
                        thickness = 8.dp,
                        xValueFormatter = CartesianValueFormatter { context, x, _ ->
                            context.model.extraStore[lastMonthSummaryChartData.second][x.toInt()]
                        }
                    )
                }
            }

            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .clip(bottomListItemShape)
                        .background(listItemColors.containerColor)
                        .clickable {}
                        .padding(
                            start = 20.dp,
                            top = 20.dp,
                            bottom = 20.dp
                        ) // end = 0 to let the chart touch the end
                ) {
                    Text(
                        stringResource(R.string.last_year),
                        style = typography.headlineSmall
                    )

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            millisecondsToHoursMinutes(
                                remember(lastYearAverageFocusTimes) {
                                    lastYearAverageFocusTimes.sum().toLong()
                                },
                                hoursMinutesFormat
                            ),
                            style = typography.displaySmall
                        )
                        Text(
                            text = stringResource(R.string.focus_per_day_avg),
                            style = typography.titleSmall,
                            modifier = Modifier.padding(bottom = 5.2.dp)
                        )
                    }

                    TimeLineChart(
                        modelProducer = lastYearSummaryChartData.first,
                        hoursFormat = hoursFormat,
                        hoursMinutesFormat = hoursMinutesFormat,
                        minutesFormat = minutesFormat,
                        axisTypeface = axisTypeface,
                        markerTypeface = markerTypeface,
                        xValueFormatter = CartesianValueFormatter { context, x, _ ->
                            context.model.extraStore[lastYearSummaryChartData.second][x.toInt()]
                        }
                    )
                }
            }
        }
    }
}