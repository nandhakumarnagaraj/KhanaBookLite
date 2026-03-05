package com.khanabook.lite.pos

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import android.app.Application
import android.util.Log
import com.khanabook.lite.pos.data.local.DatabaseInitializer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@HiltAndroidApp
class KhanaBookApplication : Application() {

    @Inject lateinit var databaseInitializer: DatabaseInitializer

    override fun onCreate() {
        super.onCreate()

        // Initialize Global Crash Handler
        com.khanabook.lite.pos.domain.util.GlobalCrashHandler.initialize(this)

        // Initialize sample data on startup
        val exceptionHandler =
                kotlinx.coroutines.CoroutineExceptionHandler { _, throwable ->
                    Log.e("KhanaBookApp", "Coroutine Exception", throwable)
                }

        MainScope().launch(exceptionHandler) {
            try {
                if (::databaseInitializer.isInitialized) {
                    databaseInitializer.initialize()
                }
            } catch (e: Exception) {
                Log.e("KhanaBookApp", "Failed to initialize database", e)
            }
        }
    }
}


