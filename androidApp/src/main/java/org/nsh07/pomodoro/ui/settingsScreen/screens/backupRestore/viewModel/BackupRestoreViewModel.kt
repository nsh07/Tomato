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

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.lifecycle.ViewModel
import androidx.sqlite.db.SimpleSQLiteQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.nsh07.pomodoro.data.AppDatabase
import org.nsh07.pomodoro.data.SystemDao
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME

class BackupRestoreViewModel(
    val systemDao: SystemDao,
    val database: AppDatabase
) : ViewModel() {
    suspend fun performBackup(context: Context, uri: Uri) {
        withContext(Dispatchers.IO) {
            systemDao.checkpoint(SimpleSQLiteQuery("pragma wal_checkpoint(full)"))

            val dbName = "app_database"
            val dbFile = context.getDatabasePath(dbName)

            val documentId = DocumentsContract.getTreeDocumentId(uri)
            val parentDocumentUri = DocumentsContract.buildDocumentUriUsingTree(uri, documentId)

            val fileUri = DocumentsContract.createDocument(
                context.contentResolver,
                parentDocumentUri,
                "application/octet-stream", // MIME type
                "tomato-backup-${LocalDateTime.now().format(ISO_LOCAL_DATE_TIME)}.db"
            )

            fileUri?.let {
                context.contentResolver.openOutputStream(it)?.use { output ->
                    FileInputStream(dbFile).use { input ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }

    suspend fun performRestore(context: Context, backupUri: Uri) {
        withContext(Dispatchers.IO) {
            database.close()

            val dbName = "app_database"
            val dbFile = context.getDatabasePath(dbName)

            if (!dbFile.parentFile!!.exists()) dbFile.parentFile!!.mkdirs()

            File("${dbFile.path}-wal").delete()
            File("${dbFile.path}-shm").delete()

            context.contentResolver.openInputStream(backupUri)?.use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }
        }
    }
}