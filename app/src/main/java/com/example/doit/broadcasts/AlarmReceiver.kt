package com.example.doit.broadcasts

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.doit.sendNotification
import com.example.doit.todoList.createTodo.CreateTodoViewModel.Companion.CHANNEL_EXTRA
import com.example.doit.todoList.createTodo.CreateTodoViewModel.Companion.NOTIFICATION_EXTRA
import com.example.doit.todoList.createTodo.CreateTodoViewModel.Companion.TODO_STRING_EXTRA

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        intent.getStringExtra(CHANNEL_EXTRA)?.let { channel ->
            ContextCompat.getSystemService(context, NotificationManager::class.java)
                ?.sendNotification(
                    context,
                    intent.getIntExtra(
                        NOTIFICATION_EXTRA, 0
                    ), channel,
                    intent.getStringExtra(TODO_STRING_EXTRA) ?: "Empty todo"
                )
        } ?: throw IllegalArgumentException()
    }
}