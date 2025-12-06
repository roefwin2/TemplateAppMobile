package org.society.appname.application

import android.app.Application
import org.society.appname.module.authentication.di.initKoin

class AppNameApplication : Application() {
    override fun onCreate() {
        super.onCreate()
         
        // Initialize Koin
        initKoin()
    }
}