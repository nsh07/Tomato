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

package org.nsh07.pomodoro.data

actual class FileLocator {
    actual val path: String?
        get() = TODO("Not yet implemented")
    actual val isNull: Boolean
        get() = TODO("Not yet implemented")

    actual constructor() {
        TODO("Not yet implemented")
    }
}

class DesktopBackupRestoreManager : BackupRestoreManager {
    override suspend fun performBackup(directoryLocator: FileLocator) {
        TODO("Not yet implemented")
    }

    override suspend fun performRestore(fileLocator: FileLocator) {
        TODO("Not yet implemented")
    }

    override fun restartApp() {
        TODO("Not yet implemented")
    }
}