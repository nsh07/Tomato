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

package org.nsh07.pomodoro.ui.settingsScreen.screens.backupRestore.viewModel

import androidx.lifecycle.ViewModel
import org.nsh07.pomodoro.data.BackupRestoreManager
import org.nsh07.pomodoro.data.FileLocator

class BackupRestoreViewModel(
    val backupRestoreManager: BackupRestoreManager
) : ViewModel() {
    suspend fun performBackup(directoryLocator: FileLocator) =
        backupRestoreManager.performBackup(directoryLocator)

    suspend fun performRestore(fileLocator: FileLocator) =
        backupRestoreManager.performRestore(fileLocator)

    fun restartApp() =
        backupRestoreManager.restartApp()
}