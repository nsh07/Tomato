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

package org.nsh07.pomodoro.reporting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.ui.htmlToAnnotatedString
import org.nsh07.pomodoro.ui.theme.LocalAppFonts
import org.nsh07.pomodoro.ui.theme.TomatoTheme
import kotlin.text.Typography.nbsp

/**
 * A one-time notice asking whether crashes, ANRs and performance data may be reported
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrashReportingNoticeSheet() {
    val context = LocalContext.current
    var visible by rememberSaveable { mutableStateOf(!CrashReporting.isNoticeShown(context)) }

    if (visible) {
        val onKeepEnabled = {
            CrashReporting.setEnabled(context, true)
            CrashReporting.setNoticeShown(context)
            visible = false
        }

        val onDisable = {
            CrashReporting.setEnabled(context, false)
            CrashReporting.setNoticeShown(context)
            visible = false
        }

        val sheetState = rememberBottomSheetState(
            initialValue = SheetValue.Expanded,
            enabledValues = setOf(SheetValue.Expanded),
            confirmValueChange = { it == SheetValue.Expanded }
        )

        ModalBottomSheet(
            onDismissRequest = {},
            properties = ModalBottomSheetProperties(
                shouldDismissOnBackPress = false,
                shouldDismissOnClickOutside = false
            ),
            sheetState = sheetState,
            containerColor = colorScheme.surfaceContainer,
            contentColor = colorScheme.onSurface
        ) {
            CrashReportingNoticeSheetBody(onKeepEnabled = onKeepEnabled, onDisable = onDisable)
        }
    }
}

@Composable
private fun CrashReportingNoticeSheetBody(
    onKeepEnabled: () -> Unit,
    onDisable: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(24.dp)
    ) {
        Icon(
            painterResource(R.drawable.avg_time_40dp),
            null,
            tint = colorScheme.secondary
        )
        Text(
            stringResource(R.string.send_diagnostics_question),
            style = typography.headlineSmall,
            color = colorScheme.onSurface
        )
        Text(
            htmlToAnnotatedString(
                stringResource(
                    R.string.crash_reporting_notice_desc,
                    "<b>${stringResource(R.string.settings)}$nbsp>$nbsp${
                        stringResource(R.string.about)
                    }</b>"
                )
            ),
            style = typography.bodyMedium,
            fontFamily = LocalAppFonts.current.annotatedString,
            color = colorScheme.onSurfaceVariant
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onKeepEnabled,
                shapes = ButtonDefaults
                    .shapesFor(ButtonDefaults.MediumContainerHeight)
                    .copy(
                        shape = RoundedCornerShape(
                            topStart = 24.dp,
                            topEnd = 24.dp,
                            bottomStart = 8.dp,
                            bottomEnd = 8.dp
                        )
                    ),
                modifier = Modifier
                    .height(ButtonDefaults.MediumContainerHeight)
                    .fillMaxWidth()
            ) {
                Icon(
                    painterResource(R.drawable.check),
                    null,
                    Modifier.size(ButtonDefaults.MediumIconSize)
                )
                Spacer(Modifier.width(ButtonDefaults.MediumIconSpacing))
                Text(
                    stringResource(R.string.allow),
                    style = typography.titleMedium
                )
            }

            FilledTonalButton(
                onClick = onDisable,
                shapes = ButtonDefaults
                    .shapesFor(ButtonDefaults.MediumContainerHeight)
                    .copy(
                        shape = RoundedCornerShape(
                            topStart = 8.dp,
                            topEnd = 8.dp,
                            bottomStart = 24.dp,
                            bottomEnd = 24.dp
                        )
                    ),
                modifier = Modifier
                    .height(ButtonDefaults.MediumContainerHeight)
                    .fillMaxWidth()
            ) {
                Icon(
                    painterResource(R.drawable.clear),
                    null,
                    Modifier.size(ButtonDefaults.MediumIconSize)
                )
                Spacer(Modifier.width(ButtonDefaults.MediumIconSpacing))
                Text(
                    stringResource(R.string.dont_allow),
                    style = typography.titleMedium
                )
            }
        }
    }
}

@Preview
@Composable
private fun CrashReportingNoticeSheetPreview() {
    TomatoTheme(dynamicColor = false) {
        Surface(color = colorScheme.surfaceContainer) {
            CrashReportingNoticeSheetBody(onKeepEnabled = {}, onDisable = {})
        }
    }
}
