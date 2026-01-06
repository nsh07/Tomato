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

package org.nsh07.pomodoro.ui.settingsScreen.screens.backupRestore

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.ui.theme.AppFonts.googleFlex600
import org.nsh07.pomodoro.ui.theme.TomatoTheme
import kotlin.text.Typography.nbsp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BackupBottomSheet(
    onDismissRequest: () -> Unit,
    onStartBackup: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedUri: Uri? by remember { mutableStateOf(null) }

    val openDirectoryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        selectedUri = uri
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(painterResource(R.drawable.backup_40dp), null, tint = colorScheme.secondary)
            Text(
                stringResource(R.string.backup),
                style = typography.headlineSmall,
                color = colorScheme.onSurface
            )
            Text(
                buildAnnotatedString {
                    append(stringResource(R.string.backup_dialog_desc, ""))
                    withStyle(SpanStyle(fontFamily = googleFlex600)) {
                        append(stringResource(R.string.settings))
                        append("$nbsp>$nbsp")
                        append(stringResource(R.string.backup_and_restore))
                        append("$nbsp>$nbsp")
                        append(stringResource(R.string.restore))
                        append('.')
                    }
                },
                style = typography.bodyMedium,
                color = colorScheme.onSurfaceVariant
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(40.dp))
                    .clickable { openDirectoryLauncher.launch(null) }
                    .background(colorScheme.surfaceVariant)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .background(colorScheme.onSurfaceVariant, shapes.extraLarge)
                        .size(48.dp)
                ) {
                    Icon(
                        painterResource(R.drawable.folder),
                        null,
                        tint = colorScheme.surfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    selectedUri?.path?.substringAfter(':')
                        ?: stringResource(R.string.choose_folder),
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.align(Alignment.End)
            ) {
                FilledTonalButton(
                    onClick = onDismissRequest,
                    shapes = ButtonDefaults.shapes()
                ) { Text(stringResource(android.R.string.cancel)) }
                Button(
                    onClick = {
                        if (selectedUri == null) openDirectoryLauncher.launch(null)
                        else onStartBackup(selectedUri!!)
                    },
                    shapes = ButtonDefaults.shapes()
                ) {
                    Text(
                        if (selectedUri == null) stringResource(R.string.choose_folder)
                        else stringResource(R.string.start_backup)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun BackupBottomSheetPreview() {
    TomatoTheme(dynamicColor = false) {
        BackupBottomSheet({}, {})
    }
}