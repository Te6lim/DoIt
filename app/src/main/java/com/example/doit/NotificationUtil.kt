package com.example.doit

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.example.doit.broadcasts.CheckTodoReceiver
import com.example.doit.todoList.createTodo.CreateTodoViewModel.Companion.CAT_ID_EXTRA
import com.example.doit.todoList.createTodo.CreateTodoViewModel.Companion.DEADLINE_CHANNEL
import com.example.doit.todoList.createTodo.CreateTodoViewModel.Companion.TIME_TODO_CHANNEL
import com.example.doit.todoList.createTodo.CreateTodoViewModel.Companion.TODO_ID_EXTRA

@SuppressLint("UnspecifiedImmutableFlag")
fun NotificationManager.sendNotification(
    context: Context,
    notificationId: Int,
    channel: String,
    message: String,
    requestCode: Int,
    bundle: Bundle
) {
    val pendingIntent = PendingIntent.getActivity(
        context, notificationId, Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    val intent = Intent(context, CheckTodoReceiver::class.java)
    val todoId = bundle.getLong(TODO_ID_EXTRA)
    val catId = bundle.getInt(CAT_ID_EXTRA)

    if (todoId != -1L && catId != -1) {
        intent.apply {
            putExtra(TODO_ID_EXTRA, todoId)
            putExtra(CAT_ID_EXTRA, catId)
        }
    }

    val donePendingIntent = PendingIntent.getBroadcast(
        context, requestCode, intent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )
    val notification = NotificationCompat.Builder(context, channel)
        .setSmallIcon(R.drawable.ic_todo)
        .setContentIntent(pendingIntent)
        .setContentTitle(
            when (channel) {
                DEADLINE_CHANNEL -> "Deadline"
                TIME_TODO_CHANNEL -> "Todo"
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