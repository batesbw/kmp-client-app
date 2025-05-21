package com.mass.client

import android.app.Application
import com.mass.client.di.allModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            // Log Koin activity - default Level.INFO. Use Level.DEBUG for more logs.
            // Use Level.ERROR or Level.NONE in production.
            androidLogger(Level.INFO)
            // Declare Android context
            androidContext(this@MainApplication)
            // Load modules
            modules(allModules)
        }
    }
} 