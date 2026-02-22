package com.dechow.owen.bible_reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

const val NOTIFICATION_ID_RELOADED = 1
const val NOTIFICATION_ID_NOT_INSTALLED = 1

fun sendNotification(
    context: Context,
    title: String,
    message: String,
    id: Int
) {
    val manager = context.getSystemService(NotificationManager::class.java)

    // Create channel on Android O+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            "bible_usage_alerts",
            "bible_usage_alerts",
            NotificationManager.IMPORTANCE_HIGH
        )
        manager.createNotificationChannel(channel)
    }

    val notification = NotificationCompat.Builder(context, "bible_usage_alerts")
        .setContentTitle(title)
        .setContentText(message)
        .setSmallIcon(R.drawable.logo)
        .setAutoCancel(true)
        .build()

    manager.notify(id, notification)
}