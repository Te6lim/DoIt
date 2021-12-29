package com.te6lim.doit.broadcasts

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.te6lim.doit.database.*
import com.te6lim.doit.todoList.createTodo.CreateTodoViewModel.Companion.NOTIFICATION_EXTRA
import com.te6lim.doit.todoList.createTodo.CreateTodoViewModel.Companion.TODO_ID_EXTRA
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import kotlin.math.roundToInt

class CheckTodoReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(Dispatchers.Default)

    override fun onReceive(context: Context, intent: Intent) {
        val todoId = intent.getLongExtra(TODO_ID_EXTRA, -1L)
        val notificationId = intent.getIntExtra(NOTIFICATION_EXTRA, 0)
        updateTodo(context, todoId, notificationId)
    }

    private fun updateTodo(context: Context, todoId: Long, notificationId: Int) {
        val todoDb = TodoDatabase.getInstance(context).databaseDao
        val catDb = CategoryDb.getInstance(context).dao
        val summaryDb = getInstance(context).summaryDao
        scope.launch {
            withContext(Dispatchers.IO) {
                val summary = summaryDb.getSummary()
                val categories = catDb.getAll()
                val todo = todoDb.get(todoId)!!.apply {
                    isFinished = true
                    dateFinished = LocalDateTime.now()
                }
                val cat = catDb.get(todo.catId)!!
                todoDb.update(todo.apply { isSuccess = isSuccess(this) })
                cat.apply {
                    if (todo.isFinished) totalFinished += 1
                    else totalFinished -= 1

                    if (todo.isSuccess)
                        totalSuccess += 1
                    else
                        totalFailure += 1
                }
                catDb.update(cat)

                if (todo.isFinished) {
                    updateFinishedCount(summary, true)
                    updateDeadlineStatus(summary, todo)
                } else updateFinishedCount(summary, false)

                updateMostActive(categories, summary, cat)
                updateLeastActive(categories, summary)
                updateMostSuccessful(categories, summary)
                updateLeastSuccessful(categories, summary)
                summaryDb.insert(summary)
                categories.forEach {
                    catDb.update(it)
                }
                ContextCompat.getSystemService(context, NotificationManager::class.java)
                    ?.cancel(notificationId)
            }
        }
    }

    private fun isSuccess(todo: Todo): Boolean {
        if (todo.hasDeadline) {
            with(todo) {
                return (dateFinished!!.toLocalDate() <= deadlineDate!!.toLocalDate()
                        && dateFinished!!.toLocalTime() <= deadlineDate!!.toLocalTime())
            }
        } else return todo.isFinished
    }

    private fun updateFinishedCount(summary: Summary, value: Boolean) {
        if (value) summary.todosFinished += 1
        else summary.todosFinished -= 1
    }

    private fun updateDeadlineStatus(summary: Summary, todo: Todo) {
        if (todo.hasDeadline) {
            if (todo.dateFinished!!.toLocalDate() <= todo.deadlineDate!!.toLocalDate()
                && todo.dateFinished!!.toLocalTime() <= todo.deadlineDate!!.toLocalTime()
            ) {
                summary.deadlinesMet += 1
            } else {
                summary.deadlinesUnmet += 1
            }
        }
    }

    private fun updateMostActive(
        categories: List<Category>,
        summary: Summary,
        cat: Category? = null
    ) {
        if (cat != null) {
            val former = categories.find { it.id == summary.mostActiveCategory }
            if (cat.totalFinished > former?.totalFinished ?: 0) {
                summary.mostActiveCategory = cat.id
            }

        } else {
            findNextMostActive(categories, summary)
        }
    }

    private fun findNextMostActive(categories: List<Category>, summary: Summary) {
        var cat: Category? = null
        categories.forEach {
            if (it.totalFinished > cat?.totalFinished ?: 0) cat = it
        }

        cat?.let {
            summary.mostActiveCategory = it.id
        }
    }

    private fun updateLeastActive(categories: List<Category>, summary: Summary) {
        var least: Category = categories[0]
        for (i in 1 until categories.size) {
            if (categories[i].totalFinished < least.totalFinished)
                least = categories[i]
        }
        if (least.totalCreated > 0) {
            val mark = with(least) { (totalFinished.toFloat() / totalCreated) * 100f }
            if (least.id != summary.mostActiveCategory && mark < 50) {
                summary.leastActiveCategory = least.id
            }
        }
    }

    private fun updateMostSuccessful(categories: List<Category>, summary: Summary) {
        var categoryId = summary.mostSuccessfulCategory
        var rate = summary.mostSuccessfulRatio
        categories.forEach {
            with(it) {
                if (totalFinished > 0
                    && ((totalSuccess.toFloat() / totalFinished) * 100.0f).roundToInt() > rate
                ) {
                    categoryId = it.id
                    rate = ((totalSuccess.toFloat() / totalFinished) * 100).roundToInt()
                }
            }
        }

        summary.apply {
            mostSuccessfulRatio = rate
            mostSuccessfulCategory = categoryId
        }
    }

    private fun updateLeastSuccessful(categories: List<Category>, summary: Summary) {
        var categoryId = summary.leastSuccessfulCategory
        var rate = 0
        categories.forEach {
            with(it) {
                if (totalFinished > 0
                    && ((totalFailure.toFloat() / totalFinished) * 100.0f).roundToInt() > rate
                ) {
                    categoryId = it.id
                    rate =
                        ((totalFailure.toFloat() / totalFinished) * 100.0f).roundToInt()

                }
            }
        }

        if (rate < 50) {
            summary.apply {
                leastSuccessfulCategory = categoryId
                leastSuccessfulRatio = 100 - rate
            }
        }
    }
}