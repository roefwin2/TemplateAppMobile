package org.society.appname.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.society.appname.MainActivity
import org.society.appname.R

class AppnameMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "AppnameFCM"
        const val CHANNEL_ID_DEFAULT = "default_channel"
        const val CHANNEL_NAME_DEFAULT = "Notifications"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "========================================")
        Log.d(TAG, "🔔 Message FCM reçu de: ${remoteMessage.from}")
        Log.d(TAG, "📦 Data: ${remoteMessage.data}")
        Log.d(TAG, "========================================")

        val data = remoteMessage.data
        val title = data["title"] ?: remoteMessage.notification?.title ?: "Notification"
        val body = data["body"] ?: remoteMessage.notification?.body ?: ""
        val type = data["type"]

        when (type) {
            // TODO: Ajoutez vos types de notifications personnalisées ici
            // "CUSTOM_TYPE" -> handleCustomNotification(data)
            else -> {
                showNotification(title, body, data)
            }
        }
    }

    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        createDefaultNotificationChannel()

        val intent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP

            // Passer les données de la notification
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID_DEFAULT)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notification)

        Log.d(TAG, "✅ Notification affichée (ID: $notificationId)")
    }

    private fun createDefaultNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            val existingChannel = notificationManager?.getNotificationChannel(CHANNEL_ID_DEFAULT)
            if (existingChannel != null) {
                return
            }

            val channel = NotificationChannel(
                CHANNEL_ID_DEFAULT,
                CHANNEL_NAME_DEFAULT,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications de l'application"
                enableVibration(true)
                setShowBadge(true)
            }

            notificationManager?.createNotificationChannel(channel)
            Log.d(TAG, "📢 Canal de notification créé")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "🔑 Nouveau FCM Token: $token")
        // Le token est géré par KMPNotifier via NotificationListener
    }
}