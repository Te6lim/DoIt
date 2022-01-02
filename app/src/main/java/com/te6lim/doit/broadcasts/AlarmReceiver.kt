package com.te6lim.doit.broadcasts

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.te6lim.doit.database.TodoDatabase
import com.te6lim.doit.sendNotification
import com.te6lim.doit.todoList.createTodo.CreateTodoViewModel.Companion.CAT_STRING_EXTRA
import com.te6lim.doit.todoList.createTodo.CreateTodoViewModel.Companion.CHANNEL_EXTRA
import com.te6lim.doit.todoList.createTodo.CreateTodoViewModel.Companion.NOTIFICATION_EXTRA
import com.te6lim.doit.todoList.createTodo.CreateTodoViewModel.Companion.TODO_ID_EXTRA
import com.te6lim.doit.todoList.createTodo.CreateTodoViewModel.Companion.TODO_STRING_EXTRA
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(NOTIFICATION_EXTRA, 0)

        intent.getStringExtra(CHANNEL_EXTRA)?.let { channel ->
            val categoryName = intent.getStringExtra(CAT_STRING_EXTRA)!!
            ContextCompat.getSystemService(context, NotificationManager::class.java)
                ?.sendNotification(
                    context,
                    notificationId, categoryName, channel,
                    intent.getStringExtra(TODO_STRING_EXTRA) ?: "Empty todo",
                    notificationId, intent.getLongExtra(TODO_ID_EXTRA, -1L)
                )
        } ?: run {
            val scope = CoroutineScope(Dispatchers.Default)

            val todoId = intent.getLongExtra(TODO_ID_EXTRA, -1L)

            val todoDb = TodoDatabase.getInstance(context).databaseDao

            scope.launch {
                withContext(Dispatchers.IO) {
                    todoDb.update(todoDb.get(todoId)!!.apply { isLate = true })
                }
            }

        }
    }
}