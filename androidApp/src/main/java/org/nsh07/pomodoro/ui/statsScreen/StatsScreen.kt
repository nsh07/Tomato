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

package org.nsh07.pomodoro.ui.statsScreen

import android.graphics.Typeface
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.unveilIn
import androidx.compose.animation.veilOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy.Companion.detailPane
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy.Companion.listPane
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import org.koin.compose.koinInject
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.ui.Screen
import org.nsh07.pomodoro.ui.calculatePaneScaffoldDirective
import org.nsh07.pomodoro.ui.settingsScreen.DetailPlaceholder
import org.nsh07.pomodoro.ui.statsScreen.screens.LastMonthScreen
import org.nsh07.pomodoro.ui.statsScreen.screens.LastWeekScreen
import org.nsh07.pomodoro.ui.statsScreen.screens.LastYearScreen
import org.nsh07.pomodoro.ui.statsScreen.screens.StatsMainScreen
import org.nsh07.pomodoro.ui.statsScreen.viewModel.StatsViewModel
import org.nsh07.pomodoro.utils.onBack

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun StatsScreenRoot(
    contentPadding: PaddingValues,
    focusGoal: Long,
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = koinInject()
) {
    val typography = typography
    val backStack = viewModel.backStack

    val todayStat by viewModel.todayStat.collectAsStateWithLifecycle(null)
    val allTimeTotalFocus by viewModel.allTimeTotalFocus.collectAsStateWithLifecycle(null)

    val lastWeekMainChartData by viewModel.lastWeekMainChartData.collectAsStateWithLifecycle()
    val lastWeekFocusHistoryValues by viewModel.lastWeekFocusHistoryValues.collectAsStateWithLifecycle()
    val lastWeekFocusBreakdownValues by viewModel.lastWeekFocusBreakdownValues.collectAsStateWithLifecycle()

    val lastMonthMainChartData by viewModel.lastMonthMainChartData.collectAsStateWithLifecycle()
    val lastMonthCalendarData by viewModel.lastMonthCalendarData.collectAsStateWithLifecycle()
    val lastMonthFocusBreakdownValues by viewModel.lastMonthFocusBreakdownValues.collectAsStateWithLifecycle()

    val lastYearMainChartData by viewModel.lastYearMainChartData.collectAsStateWithLifecycle()
    val lastYearFocusHeatmapData by viewModel.lastYearFocusHeatmapData.collectAsStateWithLifecycle()
    val lastYearFocusBreakdownValues by viewModel.lastYearFocusBreakdownValues.collectAsStateWithLifecycle()
    val lastYearMaxFocus by viewModel.lastYearMaxFocus.collectAsStateWithLifecycle()

    val colorScheme = colorScheme

    val hoursFormat = stringResource(R.string.hours_format)
    val hoursMinutesFormat = stringResource(R.string.hours_and_minutes_format)
    val minutesFormat = stringResource(R.string.minutes_format)

    val resolver = LocalFontFamilyResolver.current
    val axisTypeface =
        remember { resolver.resolve(typography.bodyMedium.fontFamily).value as Typeface }
    val markerTypeface =
        remember { resolver.resolve(typography.bodyLarge.fontFamily).value as Typeface }

    SharedTransitionLayout {
        NavDisplay(
            backStack = backStack,
            onBack = backStack::onBack,
            transitionSpec = {
                fadeIn().togetherWith(veilOut(targetColor = colorScheme.surfaceDim))
            },
            popTransitionSpec = {
                unveilIn(initialColor = colorScheme.surfaceDim).togetherWith(fadeOut())
            },
            predictivePopTransitionSpec = {
                unveilIn(initialColor = colorScheme.surfaceDim).togetherWith(fadeOut())
            },
            sceneStrategy = rememberListDetailSceneStrategy(
                directive = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo())
            ),
            entryProvider = entryProvider {
                entry<Screen.Stats.Main>(
                    metadata = listPane(detailPlaceholder = {
                        DetailPlaceholder(
                            icon = R.drawable.query_stats,
                            background = colorScheme.surface
                        )
                    })
                ) {
                    StatsMainScreen(
                        goal = focusGoal,
                        contentPadding = contentPadding,
                        lastWeekSummaryChartData = lastWeekMainChartData,
                        lastMonthSummaryChartData = lastMonthMainChartData,
                        lastYearSummaryChartData = lastYearMainChartData,
                        todayStat = todayStat,
                        allTimeTotalFocus = allTimeTotalFocus,
                        lastWeekAverageFocusTimes = lastWeekFocusBreakdownValues.first,
                        lastMonthAverageFocusTimes = lastMonthFocusBreakdownValues.first,
                        lastYearAverageFocusTimes = lastYearFocusBreakdownValues.first,
                        generateSampleData = viewModel::generateSampleData,
                        hoursFormat = hoursFormat,
                        hoursMinutesFormat = hoursMinutesFormat,
                        minutesFormat = minutesFormat,
                        axisTypeface = axisTypeface,
                        markerTypeface = markerTypeface,
                        zoomStates = viewModel.chartZoomStates,
                        scrollStates = viewModel.chartScrollStates,
                        onNavigate = {
                            if (backStack.size < 2) backStack.add(it)
                            else backStack[backStack.lastIndex] = it
                        },
                        modifier = modifier
                    )
                }

                entry<Screen.Stats.LastWeek>(
                    metadata = detailPane()
                ) {
                    LastWeekScreen(
                        goal = focusGoal,
                        contentPadding = contentPadding,
                        focusBreakdownValues = lastWeekFocusBreakdownValues,
                        focusHistoryValues = lastWeekFocusHistoryValues,
                        mainChartData = lastWeekMainChartData,
                        onBack = backStack::onBack,
                        hoursMinutesFormat = hoursMinutesFormat,
                        hoursFormat = hoursFormat,
                        minutesFormat = minutesFormat,
                        axisTypeface = axisTypeface,
                        markerTypeface = markerTypeface,
                        zoomState = viewModel.chartZoomStates[0],
                        scrollState = viewModel.chartScrollStates[0]
                    )
                }

                entry<Screen.Stats.LastMonth>(
                    metadata = detailPane()
                ) {
                    LastMonthScreen(
                        goal = focusGoal,
                        contentPadding = contentPadding,
                        focusBreakdownValues = lastMonthFocusBreakdownValues,
                        calendarData = lastMonthCalendarData,
                        mainChartData = lastMonthMainChartData,
                        onBack = backStack::onBack,
                        hoursMinutesFormat = hoursMinutesFormat,
                        hoursFormat = hoursFormat,
                        minutesFormat = minutesFormat,
                        axisTypeface = axisTypeface,
                        markerTypeface = markerTypeface,
                        zoomState = viewModel.chartZoomStates[1],
                        scrollState = viewModel.chartScrollStates[1]
                    )
                }

                entry<Screen.Stats.LastYear>(
                    metadata = detailPane()
                ) {
                    LastYearScreen(
                        goal = focusGoal,
                        contentPadding = contentPadding,
                        focusBreakdownValues = lastYearFocusBreakdownValues,
                        focusHeatmapData = lastYearFocusHeatmapData,
                        heatmapMaxValue = lastYearMaxFocus,
                        mainChartData = lastYearMainChartData,
                        onBack = backStack::onBack,
                        hoursMinutesFormat = hoursMinutesFormat,
                        hoursFormat = hoursFormat,
                        minutesFormat = minutesFormat,
                        axisTypeface = axisTypeface,
                        markerTypeface = markerTypeface,
                        zoomState = viewModel.chartZoomStates[2],
                        scrollState = viewModel.chartScrollStates[2]
                    )
                }
            }
        )
    }
}
