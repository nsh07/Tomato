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

package org.nsh07.pomodoro.ui.timerScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.nsh07.pomodoro.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmDialog(
    modifier: Modifier = Modifier,
    stopAlarm: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = stopAlarm,
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight()
                .clickable(onClick = stopAlarm),
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = AlertDialogDefaults.TonalElevation,
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Icon(
                    painter = painterResource(R.drawable.alarm),
                    contentDescription = stringResource(R.string.alarm),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.stop_alarm_question),
                    style = typography.headlineSmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.stop_alarm_dialog_text)
                )
                Spacer(modifier = Modifier.height(24.dp))
                TextButton(
                    onClick = stopAlarm,
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text(stringResource(R.string.stop_alarm))
                }
            }
        }
    }
}
