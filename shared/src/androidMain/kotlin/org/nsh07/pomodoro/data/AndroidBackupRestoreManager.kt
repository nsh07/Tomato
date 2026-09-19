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
import android.provider.DocumentsContract
import androidx.core.net.toUri
import androidx.room.RoomRawQuery
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.nsh07.pomodoro.BuildKonfig
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.time.Clock

class AndroidBackupRestoreManager(
    private val database: AppDatabase,
    private val systemDao: SystemDao,
    private val context: Context
) : BackupRestoreManager {
    override suspend fun performBackup(directory: PlatformFile) {
        withContext(Dispatchers.IO) {
            systemDao.checkpoint(RoomRawQuery("PRAGMA wal_checkpoint(full)"))

            val dbName = BuildKonfig.DATABASE_NAME
            val dbFile = context.getDatabasePath(dbName)

            val documentId = DocumentsContract.getTreeDocumentId(directory.path.toUri())
            val parentDocumentUri =
                DocumentsContract.buildDocumentUriUsingTree(directory.path.toUri(), documentId)

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

    override suspend fun performRestore(file: PlatformFile?) {
        if (file == null) return
        withContext(Dispatchers.IO) {
            database.close()

            val dbName = BuildKonfig.DATABASE_NAME
            val dbFile = context.getDatabasePath(dbName)

            if (!dbFile.parentFile!!.exists()) dbFile.parentFile!!.mkdirs()

            File("${dbFile.path}-wal").delete()
            File("${dbFile.path}-shm").delete()

            context.contentResolver.openInputStream(file.path.toUri())?.use { input ->
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