package com.example.doit.broadcasts

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.example.doit.sendNotification
import com.example.doit.todoList.createTodo.CreateTodoViewModel.Companion.CAT_IDS
import com.example.doit.todoList.createTodo.CreateTodoViewModel.Companion.CAT_ID_EXTRA
import com.example.doit.todoList.createTodo.CreateTodoViewModel.Companion.CAT_STRING_EXTRA
import com.example.doit.todoList.createTodo.CreateTodoViewModel.Companion.CHANNEL_EXTRA
import com.example.doit.todoList.createTodo.CreateTodoViewModel.Companion.NOTIFICATION_EXTRA
import com.example.doit.todoList.createTodo.CreateTodoViewModel.Companion.SUMMARY_ID
import com.example.doit.todoList.createTodo.CreateTodoViewModel.Companion.TODO_ID_EXTRA
import com.example.doit.todoList.createTodo.CreateTodoViewModel.Companion.TODO_STRING_EXTRA

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(NOTIFICATION_EXTRA, 0)
        val categoryName = intent.getStringExtra(CAT_STRING_EXTRA)!!

        val bundle = Bundle().apply {
            putLong(TODO_ID_EXTRA, intent.getLongExtra(TODO_ID_EXTRA, -1L))
            putInt(CAT_ID_EXTRA, intent.getIntExtra(CAT_ID_EXTRA, -1))
            putLong(SUMMARY_ID, intent.getLongExtra(SUMMARY_ID, -1L))
            putIntegerArrayList(CAT_IDS, intent.getIntegerArrayListExtra(CAT_IDS))
        }

        intent.getStringExtra(CHANNEL_EXTRA)?.let { channel ->
            ContextCompat.getSystemService(context, NotificationManager::class.java)
                ?.sendNotification(
                    context,
                    notificationId, categoryName, channel,
                    intent.getStringExtra(TODO_STRING_EXTRA) ?: "Empty todo", notificationId, bundle
                )
        } ?: throw IllegalArgumentException()
    }
}