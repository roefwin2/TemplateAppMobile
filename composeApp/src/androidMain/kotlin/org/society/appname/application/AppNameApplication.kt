package org.society.appname.application

import android.app.Application
import org.society.appname.di.initKoin
import com.mmk.kmpnotifier.notification.NotifierManager
import com.mmk.kmpnotifier.notification.configuration.NotificationPlatformConfiguration
import org.society.appname.R
import org.koin.android.ext.koin.androidContext


class AppNameApplication : Application() {
    override fun onCreate() {

        // Configuration KMPNotifier
        NotifierManager.initialize(
            configuration = NotificationPlatformConfiguration.Android(
                notificationIconResId = R.drawable.ic_launcher_background,
                showPushNotification = true,
            )
        )
        println("✅ KMPNotifier initialisé (Android)")

        super.onCreate()
         
        // Initialize Koin
        initKoin {
            androidContext(this@AppNameApplication)
        }
    }
}