package com.example.doit.broadcasts

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.doit.sendNotification
import com.example.doit.todoList.createTodo.CreateTodoViewModel.Companion.DEADLINE_NOTIFICATION
import com.example.doit.todoList.createTodo.CreateTodoViewModel.Companion.TODO_STRING

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ContextCompat.getSystemService(context, NotificationManager::class.java)
            ?.sendNotification(
                context,
                DEADLINE_NOTIFICATION, intent.getStringExtra(TODO_STRING) ?: "Empty todo"
            )
    }
}