package com.example.doit

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat

private const val NOTIFICATION_ID = 0

fun NotificationManager.sendNotification(context: Context, channel: String, message: String) {
    val notification = NotificationCompat.Builder(context, channel)
        .setSmallIcon(R.drawable.ic_todo)
        .setContentTitle("Todo")
        .setContentText(message)
        .build()

    notify(NOTIFICATION_ID, notification)
}