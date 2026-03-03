package com.khanabooklite.app

import android.app.Application
import android.util.Log
import com.khanabooklite.app.data.local.DatabaseInitializer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@HiltAndroidApp
class KhanaBookApplication : Application() {

    @Inject lateinit var databaseInitializer: DatabaseInitializer

    override fun onCreate() {
        super.onCreate()

        // Initialize sample data on startup
        MainScope().launch {
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
