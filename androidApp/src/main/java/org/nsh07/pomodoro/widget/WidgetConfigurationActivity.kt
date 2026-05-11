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

package org.nsh07.pomodoro.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.nsh07.pomodoro.ui.settingsScreen.screens.WidgetConfigurationScreen
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsViewModel
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.WidgetConfigurationViewModel
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.WidgetType
import org.nsh07.pomodoro.ui.theme.TomatoTheme
import org.nsh07.pomodoro.utils.toColor

class WidgetConfigurationActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private val settingsViewModel: SettingsViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Find the widget id from the intent.
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        // If this activity was started with an invalid widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val viewModel: WidgetConfigurationViewModel by viewModel { parametersOf(appWidgetId) }

        // Detect widget type
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val info = appWidgetManager.getAppWidgetInfo(appWidgetId)
        val className = info?.provider?.className ?: ""
        
        val type = when {
            className.contains("TimerWidgetReceiver") -> WidgetType.TIMER
            className.contains("TodayWidgetReceiver") -> WidgetType.TODAY
            className.contains("HistoryWidgetReceiver") -> WidgetType.HISTORY
            else -> WidgetType.UNKNOWN
        }
        viewModel.setWidgetType(type)

        setContent {
            val settingsState = settingsViewModel.settingsState.value
            val seed = settingsState.colorScheme.toColor()

            TomatoTheme(
                darkTheme = true, // Force dark theme for widget config as it's usually better for overlays
                seedColor = seed,
                blackTheme = settingsState.blackTheme
            ) {
                WidgetConfigurationScreen(
                    viewModel = viewModel,
                    onDone = {
                        viewModel.saveAllSettings()
                        val resultValue = Intent().apply {
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        }
                        setResult(RESULT_OK, resultValue)
                        finish()
                    }
                )
            }
        }

        // Set the result to CANCELED. This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)
    }
}
