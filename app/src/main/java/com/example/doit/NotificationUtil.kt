package com.example.doit

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.example.doit.broadcasts.CheckTodoReceiver
import com.example.doit.todoList.createTodo.CreateTodoViewModel.Companion.CAT_IDS
import com.example.doit.todoList.createTodo.CreateTodoViewModel.Companion.CAT_ID_EXTRA
import com.example.doit.todoList.createTodo.CreateTodoViewModel.Companion.DEADLINE_CHANNEL
import com.example.doit.todoList.createTodo.CreateTodoViewModel.Companion.NOTIFICATION_EXTRA
import com.example.doit.todoList.createTodo.CreateTodoViewModel.Companion.SUMMARY_ID
import com.example.doit.todoList.createTodo.CreateTodoViewModel.Companion.TIME_TODO_CHANNEL
import com.example.doit.todoList.createTodo.CreateTodoViewModel.Companion.TODO_ID_EXTRA

@SuppressLint("UnspecifiedImmutableFlag")
fun NotificationManager.sendNotification(
    context: Context,
    notificationId: Int,
    categoryName: String,
    channel: String,
    message: String,
    requestCode: Int,
    bundle: Bundle
) {

    val startActivityIntent = Intent(context, MainActivity::class.java).apply {
        putExtra(NOTIFICATION_EXTRA, notificationId)
    }

    val pendingIntent = PendingIntent.getActivity(
        context, notificationId, startActivityIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    val notificationActionIntent = Intent(context, CheckTodoReceiver::class.java)
    val todoId = bundle.getLong(TODO_ID_EXTRA)
    val catId = bundle.getInt(CAT_ID_EXTRA)
    val summaryId = bundle.getLong(SUMMARY_ID)
    val catIdList = bundle.getIntegerArrayList(CAT_IDS)

    if (todoId != -1L && catId != -1) {
        notificationActionIntent.apply {
            putExtra(TODO_ID_EXTRA, todoId)
            putExtra(CAT_ID_EXTRA, catId)
            putExtra(SUMMARY_ID, summaryId)
            putIntegerArrayListExtra(CAT_IDS, catIdList)
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