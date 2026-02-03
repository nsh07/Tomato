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

package org.nsh07.pomodoro.ui.statsScreen.viewModel

import androidx.compose.animation.core.spring
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.util.fastMaxBy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.patrykandpatrick.vico.compose.cartesian.VicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.VicoZoomState
import com.patrykandpatrick.vico.core.cartesian.AutoScrollCondition
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nsh07.pomodoro.BuildConfig
import org.nsh07.pomodoro.TomatoApplication
import org.nsh07.pomodoro.data.Stat
import org.nsh07.pomodoro.data.StatRepository
import org.nsh07.pomodoro.ui.Screen
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class StatsViewModel(
    private val statRepository: StatRepository
) : ViewModel() {
    val backStack = mutableStateListOf<Screen.Stats>(Screen.Stats.Main)

    val chartScrollStates = List(3) {
        VicoScrollState(
            scrollEnabled = true,
            initialScroll = Scroll.Absolute.Start,
            autoScroll = Scroll.Absolute.End,
            autoScrollCondition = AutoScrollCondition.OnModelGrowth,
            autoScrollAnimationSpec = spring(0.8f, 380f)
        )
    }

    val chartZoomStates = List(3) {
        VicoZoomState(
            zoomEnabled = true,
            initialZoom = Zoom.fixed(),
            minZoom = Zoom.min(Zoom.Content, Zoom.fixed()),
            maxZoom = Zoom.max(Zoom.fixed(10f), Zoom.Content)
        )
    }

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

    private val lastWeekSummary =
        Pair(CartesianChartModelProducer(), ExtraStore.Key<List<String>>())
    private val lastMonthSummary =
        Pair(CartesianChartModelProducer(), ExtraStore.Key<List<String>>())
    private val lastYearSummary =
        Pair(CartesianChartModelProducer(), ExtraStore.Key<List<String>>())

    private val yearDayFormatter = DateTimeFormatter.ofPattern("d MMM")

    private val lastWeekStatsFlow = statRepository.getLastNDaysStats(7).filter { it.isNotEmpty() }
    private val lastMonthStatsFlow = statRepository.getLastNDaysStats(31).filter { it.isNotEmpty() }
    private val lastYearStatsFlow = statRepository.getLastNDaysStats(365).filter { it.isNotEmpty() }

    private val _lastYearMaxFocus = MutableStateFlow(Long.MAX_VALUE)
    val lastYearMaxFocus = _lastYearMaxFocus.asStateFlow()

    val lastWeekMainChartData: StateFlow<Pair<CartesianChartModelProducer, ExtraStore.Key<List<String>>>> =
        lastWeekStatsFlow
            .map { list ->
                // reversing is required because we need ascending order while the DB returns descending order
                val reversed = list.reversed()
                val keys = reversed.map {
                    it.date.dayOfWeek.getDisplayName(
                        TextStyle.NARROW,
                        Locale.getDefault()
                    )
                }
                val values = reversed.map { it.totalFocusTime() }
                lastWeekSummary.first.runTransaction {
                    columnSeries { series(values) }
                    extras { it[lastWeekSummary.second] = keys }
                }
                lastWeekSummary
            }
            .flowOn(Dispatchers.IO)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = lastWeekSummary
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
                        listOf(
                            it.focusTimeQ1,
                            it.focusTimeQ2,
                            it.focusTimeQ3,
                            it.focusTimeQ4
                        )
                    )
                }
            }
            .flowOn(Dispatchers.IO)
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
            .flowOn(Dispatchers.IO)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = Pair(listOf(0L, 0L, 0L, 0L), 0L)
            )

    val lastMonthMainChartData: StateFlow<Pair<CartesianChartModelProducer, ExtraStore.Key<List<String>>>> =
        lastMonthStatsFlow
            .map { list ->
                val reversed = list.reversed()
                val keys = reversed.map { it.date.dayOfMonth.toString() }
                val values = reversed.map { it.totalFocusTime() }
                lastMonthSummary.first.runTransaction {
                    columnSeries { series(values) }
                    extras { it[lastMonthSummary.second] = keys }
                }
                lastMonthSummary
            }
            .flowOn(Dispatchers.IO)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = lastMonthSummary
            )

    val lastMonthCalendarData: StateFlow<List<Stat?>> =
        lastMonthStatsFlow
            .map { list ->
                val list = list.reversed()
                buildList {
                    repeat(list.first().date.dayOfWeek.value - DayOfWeek.MONDAY.value) {
                        add(null) // Make sure that the data starts with a Monday
                    }
                    addAll(list)
                }
            }
            .flowOn(Dispatchers.IO)
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
            .flowOn(Dispatchers.IO)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = Pair(listOf(0L, 0L, 0L, 0L), 0L)
            )

    val lastYearMainChartData: StateFlow<Pair<CartesianChartModelProducer, ExtraStore.Key<List<String>>>> =
        lastYearStatsFlow
            .map { list ->
                val reversed = list.reversed()
                val keys = reversed.map { it.date.format(yearDayFormatter) }
                val values = reversed.map { it.totalFocusTime() }
                lastYearSummary.first.runTransaction {
                    lineSeries { series(values) }
                    extras { it[lastYearSummary.second] = keys }
                }
                lastYearSummary
            }
            .flowOn(Dispatchers.IO)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = lastYearSummary
            )

    val lastYearFocusHeatmapData: StateFlow<List<Stat?>> =
        lastYearStatsFlow
            .map { list ->
                val list = list.reversed()
                _lastYearMaxFocus.update {
                    list.fastMaxBy {
                        it.totalFocusTime()
                    }?.totalFocusTime() ?: Long.MAX_VALUE
                }
                buildList {
                    repeat(list.first().date.dayOfWeek.value - DayOfWeek.MONDAY.value) {
                        add(null) // Make sure that the data starts with a Monday
                    }
                    list.indices.forEach {
                        if (it > 0 && list[it].date.month != list[it - 1].date.month) {
                            repeat(7) { add(null) } // Add a week gap if a new month starts
                        }
                        add(list[it])
                    }
                }
            }
            .flowOn(Dispatchers.IO)
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
            .flowOn(Dispatchers.IO)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = Pair(listOf(0L, 0L, 0L, 0L), 0L)
            )

    fun generateSampleData() {
        if (BuildConfig.DEBUG) {
            viewModelScope.launch {
                val today = LocalDate.now().plusDays(1)
                var it = today.minusDays(365)

                while (it.isBefore(today)) {
                    statRepository.insertStat(
                        Stat(
                            it,
                            (0..30 * 60 * 1000L).random(),
                            (1 * 60 * 60 * 1000L..3 * 60 * 60 * 1000L).random(),
                            (0..3 * 60 * 60 * 1000L).random(),
                            (0..1 * 60 * 60 * 1000L).random(),
                            (0..100 * 60 * 1000L).random()
                        )
                    )
                    it = it.plusDays(1)
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as TomatoApplication)
                val appStatRepository = application.container.appStatRepository

                StatsViewModel(appStatRepository)
            }
        }
    }
}