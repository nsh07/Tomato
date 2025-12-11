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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.ui.mergePaddingValues
import org.nsh07.pomodoro.ui.statsScreen.components.TimeColumnChart
import org.nsh07.pomodoro.ui.theme.AppFonts.robotoFlexTopBar
import org.nsh07.pomodoro.utils.millisecondsToHoursMinutes

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LastWeekScreen(
    contentPadding: PaddingValues,
    lastWeekAverageFocusTimes: List<Int>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    hoursMinutesFormat: String,
    lastWeekSummaryChartData: Pair<CartesianChartModelProducer, ExtraStore.Key<List<String>>>,
    hoursFormat: String,
    minutesFormat: String,
    axisTypeface: Typeface,
    markerTypeface: Typeface
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val rankList = remember(lastWeekAverageFocusTimes) {
        val sortedIndices =
            lastWeekAverageFocusTimes.indices.sortedByDescending { lastWeekAverageFocusTimes[it] }
        val ranks = MutableList(lastWeekAverageFocusTimes.size) { 0 }

        sortedIndices.forEachIndexed { rank, originalIndex ->
            ranks[originalIndex] = rank
        }

        ranks
    }

    Scaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                title = {
                    Text(stringResource(R.string.last_week), fontFamily = robotoFlexTopBar)
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
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        val insets = mergePaddingValues(innerPadding, contentPadding)
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = insets,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            item {
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
                        style = typography.displaySmall
                    )
                    Text(
                        stringResource(R.string.focus_per_day_avg),
                        style = typography.titleSmall,
                        modifier = Modifier.padding(bottom = 5.2.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
                TimeColumnChart(
                    modelProducer = lastWeekSummaryChartData.first,
                    hoursFormat = hoursFormat,
                    hoursMinutesFormat = hoursMinutesFormat,
                    minutesFormat = minutesFormat,
                    axisTypeface = axisTypeface,
                    markerTypeface = markerTypeface,
                    xValueFormatter = CartesianValueFormatter { context, x, _ ->
                        context.model.extraStore[lastWeekSummaryChartData.second][x.toInt()]
                    }
                )
            }
        }
    }
}