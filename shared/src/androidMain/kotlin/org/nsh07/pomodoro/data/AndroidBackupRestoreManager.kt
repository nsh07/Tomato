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

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.sqlite.db.SimpleSQLiteQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.time.Clock

actual data class FileLocator(val uri: Uri?) {
    actual constructor() : this(null)

    actual val path: String?
        get() = uri?.path

    actual val isNull: Boolean
        get() = uri == null
}

class AndroidBackupRestoreManager(
    private val database: AppDatabase,
    private val systemDao: SystemDao,
    private val context: Context
) : BackupRestoreManager {
    override suspend fun performBackup(directoryLocator: FileLocator) {
        withContext(Dispatchers.IO) {
            systemDao.checkpoint(SimpleSQLiteQuery("pragma wal_checkpoint(full)"))

            val dbName = "app_database"
            val dbFile = context.getDatabasePath(dbName)

            val documentId = DocumentsContract.getTreeDocumentId(directoryLocator.uri)
            val parentDocumentUri =
                DocumentsContract.buildDocumentUriUsingTree(directoryLocator.uri, documentId)

            val fileUri = DocumentsContract.createDocument(
                context.contentResolver,
                parentDocumentUri,
                "application/octet-stream", // MIME type
                "tomato-backup-${Clock.System.now()}.db"
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

    override suspend fun performRestore(fileLocator: FileLocator) {
        if (fileLocator.isNull) return
        withContext(Dispatchers.IO) {
            database.close()

            val dbName = "app_database"
            val dbFile = context.getDatabasePath(dbName)

            if (!dbFile.parentFile!!.exists()) dbFile.parentFile!!.mkdirs()

            File("${dbFile.path}-wal").delete()
            File("${dbFile.path}-shm").delete()

            context.contentResolver.openInputStream(fileLocator.uri!!)?.use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }
        }
    }

    override fun restartApp() {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        val componentName = intent?.component

        val mainIntent = Intent.makeRestartActivityTask(componentName)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)

        context.startActivity(mainIntent)
        Runtime.getRuntime().exit(0)
    }
}