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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.ui.mergePaddingValues
import org.nsh07.pomodoro.ui.settingsScreen.components.ClickableListItem
import org.nsh07.pomodoro.ui.theme.AppFonts.robotoFlexTopBar
import org.nsh07.pomodoro.ui.theme.CustomColors.listItemColors
import org.nsh07.pomodoro.ui.theme.CustomColors.topBarColors
import org.nsh07.pomodoro.ui.theme.TomatoTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BackupRestoreScreen(
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    var showDialog by remember { mutableIntStateOf(0) }

    var backupState by remember { mutableStateOf(BackupRestoreState.CHOOSE_FILE) }


    if (showDialog == 1) BackupBottomSheet(
        backupState = backupState,
        onDismissRequest = { showDialog = 0 },
        onStartBackup = {
            scope.launch {
                backupState = BackupRestoreState.LOADING
                delay(1000)
                backupState = BackupRestoreState.DONE
            }
        },
        resetBackupState = { backupState = BackupRestoreState.CHOOSE_FILE }
    )

    Scaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                title = {
                    Text(stringResource(R.string.backup_and_restore), fontFamily = robotoFlexTopBar)
                },
                subtitle = {
                    Text(stringResource(R.string.settings))
                },
                navigationIcon = {
                    FilledTonalIconButton(
                        onClick = onBack,
                        shapes = IconButtonDefaults.shapes(),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = listItemColors.containerColor)
                    ) {
                        Icon(
                            painterResource(R.drawable.arrow_back),
                            stringResource(R.string.back)
                        )
                    }
                },
                colors = topBarColors,
                scrollBehavior = scrollBehavior
            )
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        val insets = mergePaddingValues(innerPadding, contentPadding)
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            contentPadding = insets,
            modifier = Modifier
                .background(topBarColors.containerColor)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(Modifier.height(14.dp))
            }

            item {
                ClickableListItem(
                    headlineContent = { Text(stringResource(R.string.backup)) },
                    supportingContent = { Text(stringResource(R.string.backup_desc)) },
                    leadingContent = { Icon(painterResource(R.drawable.backup), null) },
                    items = 2,
                    index = 0
                ) { showDialog = 1 }
            }
            item {
                ClickableListItem(
                    headlineContent = { Text(stringResource(R.string.restore)) },
                    supportingContent = { Text(stringResource(R.string.restore_desc)) },
                    leadingContent = { Icon(painterResource(R.drawable.restore), null) },
                    items = 2,
                    index = 1
                ) { }
            }
        }
    }
}

@Preview
@Composable
fun BackupRestoreScreenPreview() {
    TomatoTheme(dynamicColor = false) {
        BackupRestoreScreen(
            contentPadding = PaddingValues(),
            onBack = {}
        )
    }
}