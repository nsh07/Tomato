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

package org.nsh07.pomodoro

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ApplicationScope
import com.kdroid.composetray.tray.api.Tray
import kotlinx.coroutines.flow.update
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.nsh07.pomodoro.data.StateRepository
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerAction
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerMode
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerViewModel
import tomato.shared.generated.resources.Res
import tomato.shared.generated.resources.app_name
import tomato.shared.generated.resources.focus
import tomato.shared.generated.resources.infinite
import tomato.shared.generated.resources.logo
import tomato.shared.generated.resources.long_break
import tomato.shared.generated.resources.min_remaining_notification
import tomato.shared.generated.resources.open_tomato
import tomato.shared.generated.resources.paused
import tomato.shared.generated.resources.quit_tomato
import tomato.shared.generated.resources.restart
import tomato.shared.generated.resources.short_break
import tomato.shared.generated.resources.skip
import tomato.shared.generated.resources.start
import tomato.shared.generated.resources.stop
import tomato.shared.generated.resources.stop_alarm
import tomato.shared.generated.resources.tomato_logo_notification
import kotlin.text.Typography.middleDot

@Composable
fun ApplicationScope.AppSystemTray(
    timerViewModel: TimerViewModel = koinViewModel(),
    stateRepository: StateRepository = koinInject()
) {
    val timerState by timerViewModel.timerState.collectAsState()

    val openTomato = stringResource(Res.string.open_tomato)
    val quitTomato = stringResource(Res.string.quit_tomato)
    val infiniteString = stringResource(Res.string.infinite)
    val pausedString = stringResource(Res.string.paused)
    val stopString = stringResource(Res.string.stop)
    val startString = stringResource(Res.string.start)
    val restartString = stringResource(Res.string.restart)
    val skipString = stringResource(Res.string.skip)
    val stopAlarmString = stringResource(Res.string.stop_alarm)

    val timerModeStr = when (timerState.timerMode) {
        TimerMode.SHORT_BREAK -> stringResource(Res.string.short_break)
        TimerMode.LONG_BREAK -> stringResource(Res.string.long_break)
        else -> stringResource(Res.string.focus)
    }

    val remainingTime by stateRepository.time.collectAsState()
    val remainingTimeS by remember {
        derivedStateOf {
            if ((remainingTime.toFloat() / 60000f) < 1.0f) "< 1"
            else (remainingTime.toFloat() / 60000f).toInt()
        }
    }
    val remainingTimeStr = stringResource(Res.string.min_remaining_notification, remainingTimeS)

    Tray(
        windowsIcon = Res.drawable.logo,
        macLinuxIcon = vectorResource(Res.drawable.tomato_logo_notification),
        tooltip = stringResource(Res.string.app_name),
        primaryAction = { stateRepository.windowVisible.update { true } }
    ) {
        if (!timerState.alarmRinging) {
            SubMenu(
                "$timerModeStr $middleDot ${
                    if (timerState.timerMode == TimerMode.FOCUS && timerState.infiniteFocus) infiniteString
                    else remainingTimeStr
                }" + if (!timerState.timerRunning) " $middleDot $pausedString" else ""
            ) {
                Item(if (timerState.timerRunning) stopString else startString) {
                    timerViewModel.onAction(
                        TimerAction.ToggleTimer
                    )
                }
                Item(restartString) { timerViewModel.onAction(TimerAction.ResetTimer) }
                Item(skipString) { timerViewModel.onAction(TimerAction.SkipTimer(true)) }
            }
        } else {
            Item(stopAlarmString) { timerViewModel.onAction(TimerAction.StopAlarm) }
        }
        Divider()
        Item(openTomato) { stateRepository.windowVisible.update { true } }
        Item(quitTomato, onClick = ::exitApplication)
    }
}