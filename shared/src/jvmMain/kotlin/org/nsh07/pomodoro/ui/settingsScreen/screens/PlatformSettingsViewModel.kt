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

package org.nsh07.pomodoro.ui.settingsScreen.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nsh07.pomodoro.data.PreferenceRepository
import org.nsh07.pomodoro.data.StateRepository

class PlatformSettingsViewModel(
    private val preferenceRepository: PreferenceRepository,
    stateRepository: StateRepository
) : ViewModel() {
    private val _settingsState = stateRepository.settingsState
    val settingsState = _settingsState.asStateFlow()

    fun saveCustomWindowDecor(customWindowDecor: Boolean) {
        viewModelScope.launch {
            _settingsState.update { currentState ->
                currentState.copy(customWindowDecor = customWindowDecor)
            }
            preferenceRepository.saveBooleanPreference("custom_window_decor", customWindowDecor)
        }
    }
}