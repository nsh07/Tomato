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

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.nsh07.pomodoro.data.FileLocator
import org.nsh07.pomodoro.ui.htmlToAnnotatedString
import org.nsh07.pomodoro.ui.settingsScreen.screens.backupRestore.viewModel.BackupRestoreState
import org.nsh07.pomodoro.ui.theme.TomatoTheme
import tomato.shared.generated.resources.Res
import tomato.shared.generated.resources.backup
import tomato.shared.generated.resources.backup_40dp
import tomato.shared.generated.resources.backup_and_restore
import tomato.shared.generated.resources.backup_dialog_desc
import tomato.shared.generated.resources.choose_file
import tomato.shared.generated.resources.choose_folder
import tomato.shared.generated.resources.exit
import tomato.shared.generated.resources.restart_app
import tomato.shared.generated.resources.restore
import tomato.shared.generated.resources.restore_40dp
import tomato.shared.generated.resources.restore_dialog_desc
import tomato.shared.generated.resources.settings
import kotlin.text.Typography.nbsp

@Composable
fun BackupBottomSheet(
    backupState: BackupRestoreState,
    onDismissRequest: () -> Unit,
    onStartBackup: (FileLocator) -> Unit,
    resetBackupState: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFileLocator: FileLocator by remember { mutableStateOf(FileLocator()) }

    val launchDirectoryPicker = rememberDirectoryPickerLauncher { fileLocator ->
        selectedFileLocator = fileLocator
        resetBackupState()
    }

    BackupBottomSheetTemplate(
        backupState = backupState,
        onDismissRequest = onDismissRequest,
        onStartAction = onStartBackup,
        resetBackupState = resetBackupState,
        openPicker = launchDirectoryPicker,
        icon = {
            Icon(
                painterResource(Res.drawable.backup_40dp),
                null,
                tint = colorScheme.secondary
            )
        },
        titleText = stringResource(Res.string.backup),
        labelText = htmlToAnnotatedString(
            stringResource(
                Res.string.backup_dialog_desc,
                "<b>${stringResource(Res.string.settings)}$nbsp>$nbsp${
                    stringResource(Res.string.backup_and_restore)
                }$nbsp>$nbsp${stringResource(Res.string.restore)}</b>"
            )
        ),
        buttonText = if (backupState == BackupRestoreState.DONE) stringResource(Res.string.exit)
        else if (selectedFileLocator.isNull) stringResource(Res.string.choose_folder)
        else stringResource(Res.string.backup),
        selectedFileLocator = selectedFileLocator,
        modifier = modifier
    )
}

@Composable
fun RestoreBottomSheet(
    restoreState: BackupRestoreState,
    onDismissRequest: () -> Unit,
    onStartRestore: (FileLocator) -> Unit,
    resetRestoreState: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFileLocator: FileLocator by remember { mutableStateOf(FileLocator()) }

    val launchFilePicker = rememberFilePickerLauncher("application/octet-stream") { locator ->
        selectedFileLocator = locator
        resetRestoreState()
    }

    BackupBottomSheetTemplate(
        backupState = restoreState,
        onDismissRequest = onDismissRequest,
        onStartAction = onStartRestore,
        resetBackupState = resetRestoreState,
        openPicker = launchFilePicker,
        icon = {
            Icon(
                painterResource(Res.drawable.restore_40dp),
                null,
                tint = colorScheme.secondary
            )
        },
        titleText = stringResource(Res.string.restore),
        labelText = htmlToAnnotatedString(stringResource(Res.string.restore_dialog_desc)),
        buttonText = if (restoreState == BackupRestoreState.DONE) stringResource(Res.string.restart_app)
        else if (selectedFileLocator.isNull) stringResource(Res.string.choose_file)
        else stringResource(Res.string.restore),
        selectedFileLocator = selectedFileLocator,
        modifier = modifier
    )
}

@Preview
@Composable
fun BackupBottomSheetPreview() {
    var state by remember { mutableStateOf(BackupRestoreState.CHOOSE_FILE) }
    TomatoTheme(dynamicColor = false) {
        BackupBottomSheet(state, {}, {}, {})
    }

    LaunchedEffect(state) {
        delay(3000)
        state = when (state) {
            BackupRestoreState.CHOOSE_FILE -> BackupRestoreState.LOADING
            BackupRestoreState.LOADING -> BackupRestoreState.DONE
            else -> BackupRestoreState.CHOOSE_FILE
        }
    }
}
