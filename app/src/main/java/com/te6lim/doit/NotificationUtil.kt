package com.te6lim.doit

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.te6lim.doit.broadcasts.CheckTodoReceiver
import com.te6lim.doit.todoList.createTodo.CreateTodoViewModel.Companion.DEADLINE_CHANNEL
import com.te6lim.doit.todoList.createTodo.CreateTodoViewModel.Companion.NOTIFICATION_EXTRA
import com.te6lim.doit.todoList.createTodo.CreateTodoViewModel.Companion.TIME_TODO_CHANNEL
import com.te6lim.doit.todoList.createTodo.CreateTodoViewModel.Companion.TODO_ID_EXTRA

@SuppressLint("UnspecifiedImmutableFlag")
fun NotificationManager.sendNotification(
    context: Context,
    notificationId: Int,
    categoryName: String,
    channel: String,
    message: String,
    requestCode: Int,
    todoId: Long
) {

    val startActivityIntent = Intent(context, MainActivity::class.java).apply {
        putExtra(NOTIFICATION_EXTRA, notificationId)
    }

    val pendingIntent = PendingIntent.getActivity(
        context, requestCode, startActivityIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    val notificationActionIntent = Intent(context, CheckTodoReceiver::class.java)

    if (todoId != -1L) {
        notificationActionIntent.apply {
            putExtra(TODO_ID_EXTRA, todoId)
        }
    }

    notificationActionIntent.putExtra(NOTIFICATION_EXTRA, notificationId)

    val donePendingIntent = PendingIntent.getBroadcast(
        context, requestCode, notificationActionIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )
    val notification = NotificationCompat.Builder(context, channel)
        .setSmallIcon(R.drawable.ic_todo)
        .setContentIntent(pendingIntent)
        .setContentTitle(
            when (channel) {
                DEADLINE_CHANNEL ->
                    context.getString(R.string.notification_deadline_title, categoryName)
                TIME_TODO_CHANNEL ->
                    context.getString(R.string.notification_todo_title, categoryName)
                else -> ""
            }
        )
        .setContentText(message)

    when (channel) {
        DEADLINE_CHANNEL -> notification.addAction(
            R.drawable.ic_todo, "Done", donePendingIntent
        )
    }

    notify(notificationId, notification.build())
}