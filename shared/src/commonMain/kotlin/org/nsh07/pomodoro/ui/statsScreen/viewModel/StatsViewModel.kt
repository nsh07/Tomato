/*
 * Copyright (c) 2026 Nishant Mishra
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

package org.nsh07.pomodoro.ui.statsScreen.viewModel

import androidx.compose.animation.core.spring
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.util.fastMaxBy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrykandpatrick.vico.compose.cartesian.AutoScrollCondition
import com.patrykandpatrick.vico.compose.cartesian.Scroll
import com.patrykandpatrick.vico.compose.cartesian.VicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.VicoZoomState
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.columnModel
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.nsh07.pomodoro.data.Stat
import org.nsh07.pomodoro.data.StatRepository
import org.nsh07.pomodoro.data.TopicRepository
import org.nsh07.pomodoro.di.AppInfo
import org.nsh07.pomodoro.ui.Screen
import org.nsh07.pomodoro.ui.settingsScreen.screens.sampleTopics
import org.nsh07.pomodoro.utils.OS
import org.nsh07.pomodoro.utils.currentOS
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/** A chart's scroll offset, as a fraction of its maximum, and its zoom factor. */
data class ChartViewport(val scrollFraction: Float = 1f, val zoom: Float = 0f)

class StatsViewModel(
    private val statRepository: StatRepository,
    private val topicRepository: TopicRepository,
    private val appInfo: AppInfo,
) : ViewModel() {
    val backStack = mutableStateListOf<Screen.Stats>(Screen.Stats.Main)

    /**
     * For the main screen only, the detail screens build their own. Two `CartesianChartHost`s
     * sharing one state object invalidate each other every frame for as long as both are composed,
     * as each writes width-derived values into it from its draw phase and reads them back there.
     */
    val summaryChartScrollStates = List(3) {
        VicoScrollState(
            scrollEnabled = true,
            initialScroll = Scroll.Absolute.End,
            autoScroll = Scroll.Absolute.End,
            autoScrollCondition = AutoScrollCondition.OnModelGrowth,
            autoScrollAnimationSpec = spring(0.8f, 380f)
        )
    }

    val summaryChartZoomStates = List(3) {
        VicoZoomState(
            zoomEnabled = currentOS == OS.ANDROID,
            initialZoom = Zoom.fixed(),
            minZoom = Zoom.min(Zoom.Content, Zoom.fixed()),
            maxZoom = Zoom.max(Zoom.fixed(10f), Zoom.Content)
        )
    }

    var summaryChartViewport = ChartViewport()
        private set

    fun captureSummaryChartViewport(screen: Screen.Stats) {
        val index = when (screen) {
            Screen.Stats.LastWeek -> 0
            Screen.Stats.LastMonth -> 1
            Screen.Stats.LastYear -> 2
            Screen.Stats.Main -> return
        }
        val scrollState = summaryChartScrollStates[index]
        val maxValue = scrollState.maxValue
        summaryChartViewport = ChartViewport(
            // A fraction because the detail chart is a different width
            scrollFraction = if (maxValue > 0f) (scrollState.value / maxValue).coerceIn(0f, 1f)
            else 1f,
            zoom = summaryChartZoomStates[index].value
        )
    }

    val lastWeekChartProducer = CartesianChartModelProducer()
    val lastWeekXLabelKey = ExtraStore.Key<List<String>>()

    val lastMonthChartProducer = CartesianChartModelProducer()
    val lastMonthXLabelKey = ExtraStore.Key<List<String>>()

    val lastYearChartProducer = CartesianChartModelProducer()
    val lastYearXLabelKey = ExtraStore.Key<List<String>>()

    private val yearDayFormatter = DateTimeFormatter.ofPattern("d MMM")

    val todayStat = statRepository
        .getTodayStat()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val allTimeTotalFocus = statRepository
        .getAllTimeTotalFocusTime()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val lastWeekStatsFlow = statRepository.getLastNDaysStats(7).filter { it.isNotEmpty() }
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), replay = 1)
    private val lastMonthStatsFlow = statRepository.getLastNDaysStats(31).filter { it.isNotEmpty() }
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), replay = 1)
    private val lastYearStatsFlow = statRepository.getLastNDaysStats(365).filter { it.isNotEmpty() }
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), replay = 1)

    init {
        viewModelScope.launch(Dispatchers.Default) {
            lastWeekStatsFlow.collect { list ->
                val reversed = list.reversed()
                val keys = reversed.map {
                    it.date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault())
                }
                val values = reversed.map { it.totalFocusTime() }

                lastWeekChartProducer.runTransaction {
                    columnModel { series(values) }
                    extras { it[lastWeekXLabelKey] = keys }
                }
            }
        }

        viewModelScope.launch(Dispatchers.Default) {
            lastMonthStatsFlow.collect { list ->
                val reversed = list.reversed()
                val keys = reversed.map { it.date.dayOfMonth.toString() }
                val values = reversed.map { it.totalFocusTime() }

                lastMonthChartProducer.runTransaction {
                    columnModel { series(values) }
                    extras { it[lastMonthXLabelKey] = keys }
                }
            }
        }

        viewModelScope.launch(Dispatchers.Default) {
            lastYearStatsFlow.collect { list ->
                val reversed = list.reversed()
                val keys = reversed.map { it.date.format(yearDayFormatter) }
                val values = reversed.map { it.totalFocusTime() }

                lastYearChartProducer.runTransaction {
                    lineModel { series(values) }
                    extras { it[lastYearXLabelKey] = keys }
                }
            }
        }
    }

    val lastYearMaxFocus: StateFlow<Long> = lastYearStatsFlow
        .map { list ->
            list.fastMaxBy { it.totalFocusTime() }?.totalFocusTime() ?: Long.MAX_VALUE
        }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Long.MAX_VALUE
        )

    val lastWeekFocusHistoryValues: StateFlow<List<Pair<String, List<Long>>>> =
        lastWeekStatsFlow
            .map { value ->
                value.reversed().map {
                    Pair(
                        it.date.dayOfWeek.getDisplayName(
                            TextStyle.NARROW,
                            Locale.getDefault()
                        ),
                        listOf(it.focusTimeQ1, it.focusTimeQ2, it.focusTimeQ3, it.focusTimeQ4)
                    )
                }
            }
            .flowOn(Dispatchers.Default)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val lastWeekFocusBreakdownValues: StateFlow<Pair<List<Long>, Long>> =
        statRepository.getLastNDaysAverageFocusTimes(7)
            .map {
                Pair(
                    listOf(
                        it?.focusTimeQ1 ?: 0L,
                        it?.focusTimeQ2 ?: 0L,
                        it?.focusTimeQ3 ?: 0L,
                        it?.focusTimeQ4 ?: 0L
                    ),
                    it?.breakTime ?: 0L
                )
            }
            .flowOn(Dispatchers.Default)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = Pair(listOf(0L, 0L, 0L, 0L), 0L)
            )

    val lastMonthCalendarData: StateFlow<List<Stat?>> =
        lastMonthStatsFlow
            .map { list ->
                val reversedList = list.reversed()
                buildList {
                    repeat(reversedList.first().date.dayOfWeek.value - DayOfWeek.MONDAY.value) {
                        add(null)
                    }
                    addAll(reversedList)
                }
            }
            .flowOn(Dispatchers.Default)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val lastMonthFocusBreakdownValues: StateFlow<Pair<List<Long>, Long>> =
        statRepository.getLastNDaysAverageFocusTimes(30)
            .map {
                Pair(
                    listOf(
                        it?.focusTimeQ1 ?: 0L,
                        it?.focusTimeQ2 ?: 0L,
                        it?.focusTimeQ3 ?: 0L,
                        it?.focusTimeQ4 ?: 0L
                    ),
                    it?.breakTime ?: 0L
                )
            }
            .flowOn(Dispatchers.Default)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = Pair(listOf(0L, 0L, 0L, 0L), 0L)
            )

    val lastYearFocusHeatmapData: StateFlow<List<Stat?>> =
        lastYearStatsFlow
            .map { list ->
                val reversedList = list.reversed()
                buildList {
                    repeat(reversedList.first().date.dayOfWeek.value - DayOfWeek.MONDAY.value) {
                        add(null)
                    }
                    reversedList.indices.forEach {
                        if (it > 0 && reversedList[it].date.month != reversedList[it - 1].date.month) {
                            repeat(7) { add(null) }
                        }
                        add(reversedList[it])
                    }
                }
            }
            .flowOn(Dispatchers.Default)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val lastYearFocusBreakdownValues: StateFlow<Pair<List<Long>, Long>> =
        statRepository.getLastNDaysAverageFocusTimes(365)
            .map {
                Pair(
                    listOf(
                        it?.focusTimeQ1 ?: 0L,
                        it?.focusTimeQ2 ?: 0L,
                        it?.focusTimeQ3 ?: 0L,
                        it?.focusTimeQ4 ?: 0L
                    ),
                    it?.breakTime ?: 0L
                )
            }
            .flowOn(Dispatchers.Default)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = Pair(listOf(0L, 0L, 0L, 0L), 0L)
            )

    fun generateSampleData() {
        if (appInfo.debug) {
            viewModelScope.launch {
                sampleTopics.take(5).forEach { topic ->
                    topicRepository.insertTopic(topic)

                    val today = LocalDate.now().plusDays(1)
                    var it = today.minusDays(365)

                    while (it.isBefore(today)) {
                        if ((0..10).random() > 2) {
                            statRepository.insertStat(
                                Stat(
                                    it,
                                    topic.id,
                                    (0..30 * 60 * 1000L).random(),
                                    (1 * 60 * 60 * 1000L..3 * 60 * 60 * 1000L).random(),
                                    (0..3 * 60 * 60 * 1000L).random(),
                                    (0..1 * 60 * 60 * 1000L).random(),
                                    (0..100 * 60 * 1000L).random()
                                )
                            )
                        }
                        it = it.plusDays(1)
                    }
                }
            }
        }
    }
}