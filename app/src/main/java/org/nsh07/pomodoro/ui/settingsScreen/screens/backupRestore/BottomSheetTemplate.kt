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
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.ui.settingsScreen.screens.backupRestore.viewModel.BackupRestoreState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BackupBottomSheetTemplate(
    backupState: BackupRestoreState,
    onDismissRequest: () -> Unit,
    onStartAction: (Uri) -> Unit,
    resetBackupState: () -> Unit,
    openPicker: () -> Unit,
    icon: @Composable () -> Unit,
    titleText: String,
    labelText: AnnotatedString,
    buttonText: String,
    selectedUri: Uri?,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    val animatedBgColor by animateColorAsState(
        targetValue = when (backupState) {
            BackupRestoreState.DONE -> colorScheme.primaryContainer
            else -> colorScheme.surfaceBright
        },
        label = "backupBackground"
    )

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = {
            resetBackupState()
            onDismissRequest()
        },
        sheetState = sheetState,
        containerColor = colorScheme.surfaceContainer,
        contentColor = colorScheme.onSurface,
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            icon()
            Text(
                titleText,
                style = typography.headlineSmall,
                color = colorScheme.onSurface
            )
            Text(
                labelText,
                style = typography.bodyMedium,
                color = colorScheme.onSurfaceVariant
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(40.dp))
                    .clickable(
                        onClick = { openPicker() },
                        enabled = backupState == BackupRestoreState.CHOOSE_FILE
                    )
                    .drawBehind { drawRect(animatedBgColor) }
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                AnimatedContent(backupState) {
                    when (it) {
                        BackupRestoreState.CHOOSE_FILE ->
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

                        BackupRestoreState.LOADING ->
                            ContainedLoadingIndicator()

                        BackupRestoreState.DONE ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .background(colorScheme.onPrimaryContainer, shapes.extraLarge)
                                    .size(48.dp)
                            ) {
                                Icon(
                                    painterResource(R.drawable.check),
                                    null,
                                    tint = colorScheme.surfaceVariant,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                    }
                }

                Text(
                    selectedUri?.path?.substringAfter(':')
                        ?: buttonText,
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
                    onClick = {
                        scope.launch {
                            sheetState.hide()
                            resetBackupState()
                            onDismissRequest()
                        }
                    },
                    shapes = ButtonDefaults.shapes()
                ) { Text(stringResource(android.R.string.cancel)) }
                Button(
                    onClick = {
                        if (backupState == BackupRestoreState.DONE) {
                            scope.launch {
                                sheetState.hide()
                                resetBackupState()
                                onDismissRequest()
                            }
                        } else if (selectedUri == null) openPicker()
                        else onStartAction(selectedUri)
                    },
                    enabled = backupState != BackupRestoreState.LOADING,
                    shapes = ButtonDefaults.shapes()
                ) {
                    AnimatedContent(buttonText) {
                        Text(it)
                    }
                }
            }
        }
    }
}