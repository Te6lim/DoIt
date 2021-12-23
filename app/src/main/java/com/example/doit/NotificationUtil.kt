package com.example.doit

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat

fun NotificationManager.sendNotification(
    context: Context, notificationId: Int, channel: String, message: String
) {
    val notification = NotificationCompat.Builder(context, channel)
        .setSmallIcon(R.drawable.ic_todo)
        .setContentTitle("Todo")
        .setContentText(message)
        .build()

    notify(notificationId, notification)
}