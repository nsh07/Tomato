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

import android.graphics.Typeface
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.togetherWith
import androidx.compose.animation.unveilIn
import androidx.compose.animation.veilOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.data.Stat
import org.nsh07.pomodoro.ui.Screen
import org.nsh07.pomodoro.ui.statsScreen.screens.LastWeekScreen
import org.nsh07.pomodoro.ui.statsScreen.screens.StatsMainScreen
import org.nsh07.pomodoro.ui.statsScreen.viewModel.StatsViewModel
import org.nsh07.pomodoro.ui.theme.AppFonts.googleFlex400
import org.nsh07.pomodoro.ui.theme.AppFonts.googleFlex600

@Composable
fun StatsScreenRoot(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = viewModel(factory = StatsViewModel.Factory)
) {
    val backStack = viewModel.backStack

    val todayStat by viewModel.todayStat.collectAsStateWithLifecycle(null)

    val lastWeekSummaryChartData by viewModel.lastWeekSummaryChartData.collectAsStateWithLifecycle()
    val lastWeekAnalysisValues by viewModel.lastWeekAverageFocusTimes.collectAsStateWithLifecycle()

    val lastMonthSummaryChartData by viewModel.lastMonthSummaryChartData.collectAsStateWithLifecycle()
    val lastMonthAnalysisValues by viewModel.lastMonthAverageFocusTimes.collectAsStateWithLifecycle()

    val lastYearSummaryChartData by viewModel.lastYearSummaryChartData.collectAsStateWithLifecycle()
    val lastYearAnalysisValues by viewModel.lastYearAverageFocusTimes.collectAsStateWithLifecycle()

    StatsScreen(
        contentPadding = contentPadding,
        backStack = backStack,
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

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun StatsScreen(
    contentPadding: PaddingValues,
    backStack: SnapshotStateList<Screen.Stats>,
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
    val hoursFormat = stringResource(R.string.hours_format)
    val hoursMinutesFormat = stringResource(R.string.hours_and_minutes_format)
    val minutesFormat = stringResource(R.string.minutes_format)

    val resolver = LocalFontFamilyResolver.current
    val axisTypeface = remember { resolver.resolve(googleFlex400).value as Typeface }
    val markerTypeface = remember { resolver.resolve(googleFlex600).value as Typeface }

    NavDisplay(
        backStack = backStack,
        onBack = backStack::removeLastOrNull,
        transitionSpec = {
            unveilIn().togetherWith(veilOut())
        },
        popTransitionSpec = {
            unveilIn().togetherWith(veilOut())
        },
        predictivePopTransitionSpec = {
            unveilIn().togetherWith(veilOut())
        },
        entryProvider = entryProvider {
            entry<Screen.Stats.Main> {
                StatsMainScreen(
                    contentPadding = contentPadding,
                    lastWeekSummaryChartData = lastWeekSummaryChartData,
                    lastMonthSummaryChartData = lastMonthSummaryChartData,
                    lastYearSummaryChartData = lastYearSummaryChartData,
                    todayStat = todayStat,
                    lastWeekAverageFocusTimes = lastWeekAverageFocusTimes,
                    lastMonthAverageFocusTimes = lastMonthAverageFocusTimes,
                    lastYearAverageFocusTimes = lastYearAverageFocusTimes,
                    generateSampleData = generateSampleData,
                    hoursFormat = hoursFormat,
                    hoursMinutesFormat = hoursMinutesFormat,
                    minutesFormat = minutesFormat,
                    axisTypeface = axisTypeface,
                    markerTypeface = markerTypeface,
                    onNavigate = {
                        if (backStack.size < 2) backStack.add(it)
                        else backStack[backStack.lastIndex] = it
                    },
                    modifier = modifier
                )
            }

            entry<Screen.Stats.LastWeek> {
                LastWeekScreen(
                    contentPadding = contentPadding,
                    lastWeekAverageFocusTimes = lastWeekAverageFocusTimes,
                    onBack = backStack::removeLastOrNull,
                    hoursMinutesFormat = hoursMinutesFormat,
                    lastWeekSummaryChartData = lastWeekSummaryChartData,
                    hoursFormat = hoursFormat,
                    minutesFormat = minutesFormat,
                    axisTypeface = axisTypeface,
                    markerTypeface = markerTypeface
                )
            }
        }
    )
}
