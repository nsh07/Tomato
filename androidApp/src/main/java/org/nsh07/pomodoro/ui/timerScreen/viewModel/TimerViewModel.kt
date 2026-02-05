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

package org.nsh07.pomodoro.ui.timerScreen.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nsh07.pomodoro.TimerStateHolder
import org.nsh07.pomodoro.data.Stat
import org.nsh07.pomodoro.data.StatRepository
import org.nsh07.pomodoro.data.StateRepository
import org.nsh07.pomodoro.service.ServiceHelper
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@OptIn(FlowPreview::class)
class TimerViewModel(
    private val serviceHelper: ServiceHelper,
    private val stateRepository: StateRepository,
    private val statRepository: StatRepository,
    private val timerStateHolder: TimerStateHolder,
) : ViewModel() {

    private val _time: MutableStateFlow<Long> = timerStateHolder.time
    val timerState: StateFlow<TimerState> = stateRepository.timerState.asStateFlow()

    val time: StateFlow<Long> = _time.asStateFlow()

    val progress = _time.combine(stateRepository.timerState) { remainingTime, uiState ->
        (uiState.totalTime.toFloat() - remainingTime) / uiState.totalTime
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            var lastDate = statRepository.getLastDate()
            val today = LocalDate.now()

            // Fills dates between today and lastDate with 0s to ensure continuous history
            if (lastDate != null) {
                while (ChronoUnit.DAYS.between(lastDate, today) > 0) {
                    lastDate = lastDate?.plusDays(1)
                    statRepository.insertStat(Stat(lastDate!!, 0, 0, 0, 0, 0))
                }
            } else {
                statRepository.insertStat(Stat(today, 0, 0, 0, 0, 0))
            }

            delay(1500)

            stateRepository.timerState.update { currentState ->
                currentState.copy(showBrandTitle = false)
            }
        }
    }

    fun onAction(action: TimerAction) {
        serviceHelper.startService(action)
    }
}