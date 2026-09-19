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

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.unveilIn
import androidx.compose.animation.veilOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy.Companion.detailPane
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy.Companion.listPane
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.patrykandpatrick.vico.compose.cartesian.AutoScrollCondition
import com.patrykandpatrick.vico.compose.cartesian.Scroll
import com.patrykandpatrick.vico.compose.cartesian.VicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.VicoZoomState
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.nsh07.pomodoro.ui.Screen
import org.nsh07.pomodoro.ui.calculatePaneScaffoldDirective
import org.nsh07.pomodoro.ui.settingsScreen.DetailPlaceholder
import org.nsh07.pomodoro.ui.statsScreen.screens.LastMonthScreen
import org.nsh07.pomodoro.ui.statsScreen.screens.LastWeekScreen
import org.nsh07.pomodoro.ui.statsScreen.screens.LastYearScreen
import org.nsh07.pomodoro.ui.statsScreen.screens.StatsMainScreen
import org.nsh07.pomodoro.ui.statsScreen.viewModel.ChartViewport
import org.nsh07.pomodoro.ui.statsScreen.viewModel.StatsViewModel
import org.nsh07.pomodoro.utils.OS
import org.nsh07.pomodoro.utils.currentOS
import org.nsh07.pomodoro.utils.onBack
import tomato.shared.generated.resources.Res
import tomato.shared.generated.resources.hours_and_minutes_format
import tomato.shared.generated.resources.hours_format
import tomato.shared.generated.resources.minutes_format
import tomato.shared.generated.resources.query_stats
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun StatsScreenRoot(
    contentPadding: PaddingValues,
    focusGoal: Long,
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = koinViewModel()
) {
    val backStack = viewModel.backStack

    val colorScheme = colorScheme

    val hoursFormat = stringResource(Res.string.hours_format)
    val hoursMinutesFormat = stringResource(Res.string.hours_and_minutes_format)
    val minutesFormat = stringResource(Res.string.minutes_format)

    var chartsVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300.milliseconds)
        chartsVisible = true
    }

    SharedTransitionLayout {
        ProvideVicoTheme(rememberM3VicoTheme()) {
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
                sceneStrategies = listOf(
                    rememberListDetailSceneStrategy(
                        directive = calculatePaneScaffoldDirective(currentWindowAdaptiveInfoV2())
                    )
                ),
                entryProvider = entryProvider {
                    entry<Screen.Stats.Main>(
                        metadata = listPane(
                            detailPlaceholder = {
                                DetailPlaceholder(
                                    icon = Res.drawable.query_stats,
                                    background = colorScheme.surface
                                )
                            }
                        )
                    ) {
                        val todayStat by viewModel.todayStat.collectAsStateWithLifecycle(null)
                        val allTimeTotalFocus by
                        viewModel.allTimeTotalFocus.collectAsStateWithLifecycle(null)

                        val lastWeekFocusBreakdownValues by viewModel.lastWeekFocusBreakdownValues.collectAsStateWithLifecycle()
                        val lastMonthFocusBreakdownValues by viewModel.lastMonthFocusBreakdownValues.collectAsStateWithLifecycle()
                        val lastYearFocusBreakdownValues by viewModel.lastYearFocusBreakdownValues.collectAsStateWithLifecycle()

                        StatsMainScreen(
                            chartsVisible = chartsVisible,
                            goal = focusGoal,
                            contentPadding = contentPadding,
                            lastWeekSummaryChartModelProducer = viewModel.lastWeekChartProducer,
                            lastWeekSummaryChartXLabelKey = viewModel.lastWeekXLabelKey,
                            lastMonthSummaryChartModelProducer = viewModel.lastMonthChartProducer,
                            lastMonthSummaryChartXLabelKey = viewModel.lastMonthXLabelKey,
                            lastYearSummaryChartModelProducer = viewModel.lastYearChartProducer,
                            lastYearSummaryChartXLabelKey = viewModel.lastYearXLabelKey,
                            todayStat = todayStat,
                            allTimeTotalFocus = allTimeTotalFocus,
                            lastWeekAverageFocusTimes = lastWeekFocusBreakdownValues.first,
                            lastMonthAverageFocusTimes = lastMonthFocusBreakdownValues.first,
                            lastYearAverageFocusTimes = lastYearFocusBreakdownValues.first,
                            generateSampleData = viewModel::generateSampleData,
                            hoursFormat = hoursFormat,
                            hoursMinutesFormat = hoursMinutesFormat,
                            minutesFormat = minutesFormat,
                            zoomStates = viewModel.summaryChartZoomStates,
                            scrollStates = viewModel.summaryChartScrollStates,
                            onNavigate = {
                                viewModel.captureSummaryChartViewport(it)
                                if (backStack.size < 2) backStack.add(it)
                                else backStack[backStack.lastIndex] = it
                            },
                            modifier = modifier
                        )
                    }

                    entry<Screen.Stats.LastWeek>(
                        metadata = detailPane()
                    ) {
                        val lastWeekFocusHistoryValues by viewModel.lastWeekFocusHistoryValues.collectAsStateWithLifecycle()
                        val lastWeekFocusBreakdownValues by viewModel.lastWeekFocusBreakdownValues.collectAsStateWithLifecycle()

                        LastWeekScreen(
                            goal = focusGoal,
                            contentPadding = contentPadding,
                            focusBreakdownValues = lastWeekFocusBreakdownValues,
                            focusHistoryValues = lastWeekFocusHistoryValues,
                            mainChartModelProducer = viewModel.lastWeekChartProducer,
                            xLabelKey = viewModel.lastWeekXLabelKey,
                            onBack = backStack::onBack,
                            hoursMinutesFormat = hoursMinutesFormat,
                            hoursFormat = hoursFormat,
                            minutesFormat = minutesFormat,
                            zoomState = rememberDetailChartZoomState(
                                viewModel.summaryChartViewport
                            ),
                            scrollState = rememberDetailChartScrollState(
                                viewModel.summaryChartViewport
                            )
                        )
                    }

                    entry<Screen.Stats.LastMonth>(
                        metadata = detailPane()
                    ) {
                        val lastMonthCalendarData by viewModel.lastMonthCalendarData.collectAsStateWithLifecycle()
                        val lastMonthFocusBreakdownValues by viewModel.lastMonthFocusBreakdownValues.collectAsStateWithLifecycle()

                        LastMonthScreen(
                            goal = focusGoal,
                            contentPadding = contentPadding,
                            focusBreakdownValues = lastMonthFocusBreakdownValues,
                            calendarData = lastMonthCalendarData,
                            mainChartModelProducer = viewModel.lastMonthChartProducer,
                            xLabelKey = viewModel.lastMonthXLabelKey,
                            onBack = backStack::onBack,
                            hoursMinutesFormat = hoursMinutesFormat,
                            hoursFormat = hoursFormat,
                            minutesFormat = minutesFormat,
                            zoomState = rememberDetailChartZoomState(
                                viewModel.summaryChartViewport
                            ),
                            scrollState = rememberDetailChartScrollState(
                                viewModel.summaryChartViewport
                            )
                        )
                    }

                    entry<Screen.Stats.LastYear>(
                        metadata = detailPane()
                    ) {
                        val lastYearFocusHeatmapData by viewModel.lastYearFocusHeatmapData.collectAsStateWithLifecycle()
                        val lastYearFocusBreakdownValues by viewModel.lastYearFocusBreakdownValues.collectAsStateWithLifecycle()
                        val lastYearMaxFocus by viewModel.lastYearMaxFocus.collectAsStateWithLifecycle()

                        LastYearScreen(
                            goal = focusGoal,
                            contentPadding = contentPadding,
                            focusBreakdownValues = lastYearFocusBreakdownValues,
                            focusHeatmapData = lastYearFocusHeatmapData,
                            heatmapMaxValue = lastYearMaxFocus,
                            mainChartModelProducer = viewModel.lastYearChartProducer,
                            xLabelKey = viewModel.lastYearXLabelKey,
                            onBack = backStack::onBack,
                            hoursMinutesFormat = hoursMinutesFormat,
                            hoursFormat = hoursFormat,
                            minutesFormat = minutesFormat,
                            zoomState = rememberDetailChartZoomState(
                                viewModel.summaryChartViewport
                            ),
                            scrollState = rememberDetailChartScrollState(
                                viewModel.summaryChartViewport
                            )
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun rememberDetailChartScrollState(viewport: ChartViewport): VicoScrollState {
    val fraction = viewport.scrollFraction
    return rememberVicoScrollState(
        scrollEnabled = true,
        // Remembered because `rememberVicoScrollState` keys its state on these.
        initialScroll = remember(fraction) {
            Scroll.Absolute { _, _, _, maxValue -> fraction * maxValue }
        },
        autoScroll = Scroll.Absolute.End,
        autoScrollCondition = AutoScrollCondition.OnModelGrowth,
        autoScrollAnimationSpec = remember { spring(0.8f, 380f) }
    )
}

@Composable
private fun rememberDetailChartZoomState(viewport: ChartViewport): VicoZoomState {
    val zoom = viewport.zoom
    return rememberVicoZoomState(
        zoomEnabled = currentOS == OS.ANDROID,
        initialZoom = remember(zoom) { if (zoom > 0f) Zoom.fixed(zoom) else Zoom.fixed() },
        minZoom = remember { Zoom.min(Zoom.Content, Zoom.fixed()) },
        maxZoom = remember { Zoom.max(Zoom.fixed(10f), Zoom.Content) }
    )
}
