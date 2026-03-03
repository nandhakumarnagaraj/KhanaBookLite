package com.khanabooklite.app

import android.app.Application
import com.khanabooklite.app.data.local.DatabaseInitializer
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class KhanaBookApplication : Application() {

    @Inject
    lateinit var databaseInitializer: DatabaseInitializer

    override fun onCreate() {
        super.onCreate()
        
        // Initialize sample data on startup
        MainScope().launch {
            try {
                if (::databaseInitializer.isInitialized) {
                    databaseInitializer.initialize()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
