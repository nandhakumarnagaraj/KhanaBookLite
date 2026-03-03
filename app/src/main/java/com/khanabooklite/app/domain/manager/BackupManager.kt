package com.khanabooklite.app.domain.manager

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class BackupManager(private val context: Context) {

    fun exportDatabase(dbName: String, exportDir: File): Boolean {
        return try {
            val dbFile = context.getDatabasePath(dbName)
            if (dbFile.exists()) {
                val destFile = File(exportDir, "${dbName}_backup_${System.currentTimeMillis()}.db")
                FileInputStream(dbFile).use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun importDatabase(dbName: String, backupFile: File): Boolean {
        return try {
            if (backupFile.exists()) {
                val dbFile = context.getDatabasePath(dbName)
                FileInputStream(backupFile).use { input ->
                    FileOutputStream(dbFile).use { output ->
                        input.copyTo(output)
                    }
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
