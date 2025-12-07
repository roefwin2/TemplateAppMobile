package org.society.appname.fcm

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.mmk.kmpnotifier.notification.NotifierManager
import com.mmk.kmpnotifier.notification.PayloadData

/**
 * Composable pour écouter les notifications dans votre UI
 */
@Composable
fun NotificationListener(onNewToken: (String) -> Unit) {
    LaunchedEffect(Unit) {
        val token = NotifierManager.getPushNotifier().getToken()
        if (token != null) {
            println("🔑 Token initial récupéré: $token")
            onNewToken(token)
        }
        
        NotifierManager.addListener(object : NotifierManager.Listener {
            override fun onNewToken(token: String) {
                println("🔑 Nouveau token FCM: $token")
                onNewToken(token)
            }

            override fun onPushNotificationWithPayloadData(
                title: String?,
                body: String?,
                data: PayloadData
            ) {
                println("🔔 Notification reçue avec données")
                println("📦 Title: $title")
                println("📦 Body: $body")
                println("📦 Data: $data")
            }

            override fun onPushNotification(title: String?, body: String?) {
                println("📬 Notification simple reçue: $title - $body")
            }
        })
    }
}