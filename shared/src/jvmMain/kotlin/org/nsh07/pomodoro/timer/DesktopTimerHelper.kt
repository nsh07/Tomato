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

package org.nsh07.pomodoro.timer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.nsh07.pomodoro.service.TimerHelper
import org.nsh07.pomodoro.service.TimerManager
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerAction

class DesktopTimerHelper(private val timerManager: TimerManager) : TimerHelper {
    // TODO: Implement DesktopTimerHelper

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onAction(action: TimerAction) {
        when (action) {
            TimerAction.ResetTimer -> scope.launch {
                timerManager.resetTimer { }
            }

            is TimerAction.SkipTimer -> scope.launch {
                timerManager.skipTimer({}, {}, {})
            }

            TimerAction.StopAlarm -> {}

            TimerAction.ToggleTimer -> scope.launch {
                timerManager.toggleTimer(scope, {}, {}, { _, _, _ -> }, {}, {}, {}, {})
            }

            TimerAction.UndoReset -> scope.launch {
                timerManager.undoReset()
            }

            is TimerAction.SetInfiniteFocus -> {
                System.err.println("Invalid action: $action")
            }
        }
    }
}