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

import androidx.room.RoomRawQuery
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.databasesDir
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.system.exitProcess
import kotlin.time.Clock

actual class FileLocator(actual val path: String?) {
    actual constructor() : this(null)

    actual val isNull: Boolean
        get() = path == null
}

class DesktopBackupRestoreManager(
    private val database: AppDatabase,
    private val systemDao: SystemDao
) : BackupRestoreManager {
    override suspend fun performBackup(directoryLocator: FileLocator) {
        withContext(Dispatchers.IO) {
            systemDao.checkpoint(RoomRawQuery("PRAGMA wal_checkpoint(full)"))

            val dbName = "app_database"
            val dbFile = File(FileKit.databasesDir.path, dbName)

            val outputFile = File(directoryLocator.path!!, "tomato-backup-${Clock.System.now()}.db")
            dbFile.copyTo(outputFile, overwrite = true)
        }
    }

    override suspend fun performRestore(fileLocator: FileLocator) {
        if (fileLocator.isNull) return
        withContext(Dispatchers.IO) {
            database.close()

            val dbName = "app_database"
            val dbFile = File(FileKit.databasesDir.path, dbName)

            if (!dbFile.parentFile!!.exists()) dbFile.parentFile!!.mkdirs()

            File("${dbFile.path}-wal").delete()
            File("${dbFile.path}-shm").delete()

            val inputFile = File(fileLocator.path!!)
            inputFile.copyTo(dbFile, overwrite = true)
        }
    }

    override fun restartApp() {
        try {
            val processInfo = ProcessHandle.current().info()
            val command = processInfo.command().orElse(null)
            val arguments = processInfo.arguments().orElse(emptyArray())

            if (command != null) {
                val commandList = mutableListOf<String>()
                commandList.add(command)
                commandList.addAll(arguments)

                ProcessBuilder(commandList).start()

                exitProcess(0)
            } else {
                System.err.println("Failed to restart: Could not resolve the execution command.")
                exitProcess(0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("Failed to restart: ${e.message}")
            exitProcess(0)
        }
    }
}