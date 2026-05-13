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

package org.nsh07.pomodoro.widget.config

import androidx.compose.material3.SliderState
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nsh07.pomodoro.service.TimerHelper

enum class WidgetType {
    TIMER, TODAY, HISTORY, UNKNOWN
}

@Immutable
data class WidgetConfigurationState(
    val opacity: Float = 1.0f,
    val backgroundRole: String = "onSecondary",
    val foregroundRole: String = "primary",
    val headerRole: String = "onPrimary",
    val skipButtonRole: String = "tertiary",
    val onSkipButtonRole: String = "onTertiary",
    val barCornerRadius: Int = 16,
    val widgetType: WidgetType = WidgetType.UNKNOWN
)

class WidgetConfigurationViewModel(
    private val timerHelper: TimerHelper
) : ViewModel() {

    private val _state = MutableStateFlow(WidgetConfigurationState())
    val state = _state.asStateFlow()

    val opacitySliderState by lazy {
        SliderState(
            value = _state.value.opacity,
            valueRange = 0f..1f
        )
    }

    init {
        viewModelScope.launch {
//            val config = widgetConfigurationDao.getConfiguration(appWidgetId)
//            if (config != null) {
//                _state.update {
//                    it.copy(
//                        opacity = config.opacity,
//                        backgroundRole = config.backgroundRole,
//                        foregroundRole = config.foregroundRole,
//                        headerRole = config.headerRole,
//                        skipButtonRole = config.skipButtonRole,
//                        onSkipButtonRole = config.onSkipButtonRole,
//                        barCornerRadius = config.barCornerRadius
//                    )
//                }
//                opacitySliderState.value = config.opacity
//            }
        }
    }

    fun setWidgetType(type: WidgetType) {
        _state.update { it.copy(widgetType = type) }
    }

    fun updateOpacityInstant(opacity: Float) {
//        opacitySliderState.value = opacity
//        _state.update { it.copy(opacity = opacity) }
//        saveSettings()
    }

    fun setBackgroundRole(role: String) {
//        _state.update { it.copy(backgroundRole = role) }
//        saveSettings()
    }

    fun setForegroundRole(role: String) {
//        _state.update { it.copy(foregroundRole = role) }
//        saveSettings()
    }

    fun setHeaderRole(role: String) {
//        _state.update { it.copy(headerRole = role) }
//        saveSettings()
    }

    fun setSkipButtonRole(role: String) {
//        _state.update { it.copy(skipButtonRole = role) }
//        saveSettings()
    }

    fun setOnSkipButtonRole(role: String) {
//        _state.update { it.copy(onSkipButtonRole = role) }
//        saveSettings()
    }

    fun setBarCornerRadius(radius: Int) {
//        _state.update { it.copy(barCornerRadius = radius) }
//        saveSettings()
    }

    private fun saveSettings(id: Int) {
//        viewModelScope.launch {
//            val s = _state.value
//            val config = WidgetConfiguration(
//                id,
//                s.opacity,
//                s.backgroundRole,
//                s.foregroundRole,
//                s.headerRole,
//                s.skipButtonRole,
//                s.onSkipButtonRole,
//                s.barCornerRadius
//            )
//            widgetConfigurationDao.insertConfiguration(config)
//            timerHelper.updateWidget(id)
//        }
    }

    /**
     * Persists the full current state (including the latest opacity slider value) to the
     * database. Call this before finishing the configuration activity to guarantee that any
     * pending slider value is written.
     */
    fun saveAllSettings(id: Int) {
//        viewModelScope.launch {
//            val s = _state.value
//            val config = WidgetConfiguration(
//                id,
//                opacitySliderState.value,
//                s.backgroundRole,
//                s.foregroundRole,
//                s.headerRole,
//                s.skipButtonRole,
//                s.onSkipButtonRole,
//                s.barCornerRadius
//            )
//            widgetConfigurationDao.insertConfiguration(config)
//        }
    }
}
