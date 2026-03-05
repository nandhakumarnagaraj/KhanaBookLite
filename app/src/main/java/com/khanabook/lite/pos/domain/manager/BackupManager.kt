package com.khanabook.lite.pos.domain.manager

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

private const val TAG = "BackupManager"

class BackupManager(private val context: Context) {

    fun exportDatabase(dbName: String, exportDir: File): Boolean {
        return try {
            val dbFile = context.getDatabasePath(dbName)
            if (dbFile.exists()) {
                val destFile = File(exportDir, "${dbName}_backup_${System.currentTimeMillis()}.db")
                FileInputStream(dbFile).use { input ->
                    FileOutputStream(destFile).use { output -> input.copyTo(output) }
                }
                true
            } else {
                Log.w(TAG, "Database file not found: $dbName")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export database", e)
            false
        }
    }

    fun importDatabase(dbName: String, backupFile: File): Boolean {
        return try {
            if (backupFile.exists()) {
                val dbFile = context.getDatabasePath(dbName)
                FileInputStream(backupFile).use { input ->
                    FileOutputStream(dbFile).use { output -> input.copyTo(output) }
                }
                true
            } else {
                Log.w(TAG, "Backup file not found: ${backupFile.path}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import database", e)
            false
        }
    }
}


