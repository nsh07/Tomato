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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.ui.settingsScreen.screens.backupRestore.viewModel.BackupRestoreState
import org.nsh07.pomodoro.ui.theme.AppFonts.googleFlex600
import org.nsh07.pomodoro.ui.theme.TomatoTheme
import kotlin.text.Typography.nbsp

@Composable
fun BackupBottomSheet(
    backupState: BackupRestoreState,
    onDismissRequest: () -> Unit,
    onStartBackup: (Uri) -> Unit,
    resetBackupState: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedUri: Uri? by remember { mutableStateOf(null) }

    val chooseFolder = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        selectedUri = uri
        resetBackupState()
    }

    BackupBottomSheetTemplate(
        backupState = backupState,
        onDismissRequest = onDismissRequest,
        onStartAction = onStartBackup,
        resetBackupState = resetBackupState,
        openPicker = { chooseFolder.launch(null) },
        icon = {
            Icon(
                painterResource(R.drawable.backup_40dp),
                null,
                tint = colorScheme.secondary
            )
        },
        titleText = stringResource(R.string.backup),
        labelText = buildAnnotatedString {
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
        buttonText = if (backupState == BackupRestoreState.DONE) stringResource(R.string.exit)
        else if (selectedUri == null) stringResource(R.string.choose_folder)
        else stringResource(R.string.backup),
        selectedUri = selectedUri,
        modifier = modifier
    )
}

@Composable
fun RestoreBottomSheet(
    restoreState: BackupRestoreState,
    onDismissRequest: () -> Unit,
    onStartRestore: (Uri) -> Unit,
    resetRestoreState: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedUri: Uri? by remember { mutableStateOf(null) }

    val chooseFile = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        selectedUri = uri
        resetRestoreState()
    }

    BackupBottomSheetTemplate(
        backupState = restoreState,
        onDismissRequest = onDismissRequest,
        onStartAction = onStartRestore,
        resetBackupState = resetRestoreState,
        openPicker = { chooseFile.launch(arrayOf("*/*")) },
        icon = {
            Icon(
                painterResource(R.drawable.restore_40dp),
                null,
                tint = colorScheme.secondary
            )
        },
        titleText = stringResource(R.string.restore),
        labelText = buildAnnotatedString {
            append(stringResource(R.string.restore_dialog_desc, ""))
            withStyle(SpanStyle(fontFamily = googleFlex600)) {
                append(stringResource(R.string.restore_dialog_desc_bold_text))
            }
        },
        buttonText = if (restoreState == BackupRestoreState.DONE) stringResource(R.string.restart_app)
        else if (selectedUri == null) stringResource(R.string.choose_file)
        else stringResource(R.string.restore),
        selectedUri = selectedUri,
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
