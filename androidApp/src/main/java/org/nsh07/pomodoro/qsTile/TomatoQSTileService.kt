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

package org.nsh07.pomodoro.qsTile

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import org.koin.android.ext.android.get
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.data.StateRepository
import org.nsh07.pomodoro.service.AndroidTimerHelper
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerAction
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerMode
import org.nsh07.pomodoro.utils.androidSdkVersionAtLeast

class TomatoQSTileService : TileService() {
    override fun onTileAdded() {
        updateTile()
    }

    override fun onStartListening() {
        updateTile()
    }

    override fun onClick() {
        super.onClick()

        val stateRepository: StateRepository = get()
        val timerRunning = stateRepository.timerState.value.timerRunning

        val serviceHelper: AndroidTimerHelper = get()
        serviceHelper.onAction(TimerAction.ToggleTimer)

        val tile = qsTile ?: return

        tile.state = if (timerRunning) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE

        if (androidSdkVersionAtLeast(29))
            tile.subtitle =
                when (stateRepository.timerState.value.timerMode) {
                    TimerMode.SHORT_BREAK -> getString(R.string.short_break)
                    TimerMode.LONG_BREAK -> getString(R.string.long_break)
                    else -> getString(R.string.focus)
                }

        tile.updateTile()
    }

    private fun updateTile() {
        val stateRepository: StateRepository = get()
        val tile = qsTile ?: return

        tile.state =
            if (stateRepository.timerState.value.timerRunning) Tile.STATE_ACTIVE
            else Tile.STATE_INACTIVE

        if (androidSdkVersionAtLeast(29))
            tile.subtitle =
                when (stateRepository.timerState.value.timerMode) {
                    TimerMode.SHORT_BREAK -> getString(R.string.short_break)
                    TimerMode.LONG_BREAK -> getString(R.string.long_break)
                    else -> getString(R.string.focus)
                }

        tile.updateTile()
    }
}