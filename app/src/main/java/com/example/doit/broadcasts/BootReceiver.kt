package com.example.doit.broadcasts

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.core.app.AlarmManagerCompat
import com.example.doit.database.*
import com.example.doit.todoList.createTodo.CreateTodoViewModel
import com.example.doit.todoList.toMilliSeconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

open class BootReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(Dispatchers.Default)

    override fun onReceive(context: Context?, intent: Intent?) {
        Toast.makeText(context!!, "Boot complete", Toast.LENGTH_SHORT).show()
        Log.d("XYZ", "device boot completed")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val todoDb = TodoDatabase.getInstance(context).databaseDao
        val catDb = CategoryDb.getInstance(context).dao
        val summaryDb = getInstance(context).summaryDao
        scope.launch {
            withContext(Dispatchers.IO) {
                val todoList = todoDb.getAll()
                val catList = catDb.getAll()
                val summary = summaryDb.getSummary()
                todoList!!.forEach { todo ->
                    setTimeTodoAlarm(
                        context, alarmManager, todo,
                        catList.find { it.id == todo.catId }!!.name,
                        Integer.MAX_VALUE - todo.todoId
                    )
                    if (todo.hasDeadline)
                        setDeadlineAlarm(
                            context, alarmManager, todo,
                            catList.find { it.id == todo.catId }!!.name, catList, summary,
                            todo.todoId
                        )
                }
            }
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun setTimeTodoAlarm(
        context: Context, alarmManager: AlarmManager, todo: Todo, categoryName: String, id: Long
    ) {
        if (todo.dateTodo > LocalDateTime.now()) {
            val notifyIntent = Intent(
                context, AlarmReceiver::class.java
            ).apply {
                putExtra(CreateTodoViewModel.TODO_STRING_EXTRA, todo.todoString)
                putExtra(CreateTodoViewModel.CHANNEL_EXTRA, CreateTodoViewModel.TIME_TODO_CHANNEL)
                putExtra(CreateTodoViewModel.CAT_STRING_EXTRA, categoryName)
                putExtra(CreateTodoViewModel.NOTIFICATION_EXTRA, id)
            }
            val duration = todo.dateTodo.toMilliSeconds() - LocalDateTime.now().toMilliSeconds()
            val pendingIntent = PendingIntent.getBroadcast(
                context, id.toInt(), notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
            )
            AlarmManagerCompat.setExactAndAllowWhileIdle(
                alarmManager, AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + duration, pendingIntent
            )
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun setDeadlineAlarm(
        context: Context, alarmManager: AlarmManager, todo: Todo,
        categoryName: String, categories: List<Category>, summary: Summary, id: Long
    ) {
        val duration = todo.deadlineDate!!
            .toMilliSeconds() - LocalDateTime.now().toMilliSeconds() - CreateTodoViewModel.minute
        if (duration > 0) {
            val categoryIdList = arrayListOf<Int>()
            categories.forEach {
                categoryIdList.add(it.id)
            }
            val notifyIntent = Intent(
                context, AlarmReceiver::class.java
            ).apply {
                putExtra(CreateTodoViewModel.TODO_STRING_EXTRA, todo.todoString)
                putExtra(CreateTodoViewModel.CHANNEL_EXTRA, CreateTodoViewModel.DEADLINE_CHANNEL)
                putExtra(CreateTodoViewModel.NOTIFICATION_EXTRA, id)
                putExtra(CreateTodoViewModel.TODO_ID_EXTRA, id)
                putExtra(CreateTodoViewModel.CAT_STRING_EXTRA, categoryName)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context, id.toInt(),
                notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
            )
            AlarmManagerCompat.setExactAndAllowWhileIdle(
                alarmManager, AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + duration, pendingIntent
            )
        }
    }
}