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
 */package org.nsh07.pomodoro.ui.timerScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.nsh07.pomodoro.ui.theme.TomatoTheme
import tomato.shared.generated.resources.Res
import tomato.shared.generated.resources.alarm
import tomato.shared.generated.resources.stop_alarm
import tomato.shared.generated.resources.stop_alarm_dialog_text
import tomato.shared.generated.resources.stop_alarm_question

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AlarmDialog(
    modifier: Modifier = Modifier,
    stopAlarm: () -> Unit
) {
    Dialog(
        onDismissRequest = stopAlarm,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .fillMaxSize()
                .background(colorScheme.primaryContainer)
                .clickable(onClick = stopAlarm)
        ) {
            CompositionLocalProvider(LocalContentColor provides colorScheme.onPrimaryContainer) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Icon(
                        painter = painterResource(Res.drawable.alarm),
                        contentDescription = stringResource(Res.string.alarm),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(40.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = stringResource(Res.string.stop_alarm_question),
                        style = typography.headlineSmall,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = stringResource(Res.string.stop_alarm_dialog_text),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = stopAlarm,
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.align(Alignment.End),
                    ) {
                        Text(stringResource(Res.string.stop_alarm))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun AlarmDialogPreview() {
    TomatoTheme {
        AlarmDialog(stopAlarm = {})
    }
}
