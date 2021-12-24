package com.example.doit.broadcasts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.doit.database.*
import com.example.doit.todoList.createTodo.CreateTodoViewModel.Companion.CAT_ID_EXTRA
import com.example.doit.todoList.createTodo.CreateTodoViewModel.Companion.TODO_ID_EXTRA
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class CheckTodoReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(Dispatchers.Default)


    override fun onReceive(context: Context, intent: Intent) {
        val todoId = intent.getLongExtra(TODO_ID_EXTRA, -1L)
        val catId = intent.getIntExtra(CAT_ID_EXTRA, -1)
        updateTodo(context, todoId, catId)
    }

    private fun updateTodo(context: Context, todoId: Long, catId: Int) {
        val todoDb = TodoDatabase.getInstance(context).databaseDao
        val catDb = CategoryDb.getInstance(context).dao
        val summaryDb = getInstance(context).summaryDao
        scope.launch {
            withContext(Dispatchers.IO) {
                val todo = todoDb.get(todoId)!!.apply {
                    isFinished = true
                    dateFinished = LocalDateTime.now()
                }
                val cat = catDb.get(catId)!!
                todoDb.update(todo.apply { isSuccess = isSuccess(this) })
                //resetItemsState(todoList.value!!)
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
                    updateFinishedCount(summaryDb, true)
                    updateDeadlineStatus(summaryDb, todo)
                } else updateFinishedCount(summaryDb, false)

                updateMostActive(catDb, summaryDb, cat)
                updateLeastActive(catDb, summaryDb)
                updateMostSuccessful(catDb, summaryDb)
                updateLeastSuccessful(catDb, summaryDb)
            }
        }
    }

    private fun isSuccess(todo: Todo): Boolean {
        if (todo.hasDeadline) {
            with(todo) {
                return (dateFinished!!.toLocalDate().compareTo(deadlineDate!!.toLocalDate()) <= 0
                        && dateFinished!!.toLocalTime()
                    .compareTo(deadlineDate!!.toLocalTime()) <= 0)
            }
        } else return todo.isFinished
    }

    private fun updateFinishedCount(summaryDb: SummaryDao, value: Boolean) {
        scope.launch {
            withContext(Dispatchers.IO) {
                val summary = summaryDb.getSummary()
                if (value)
                    summaryDb.insert(summary.value!!.apply {
                        todosFinished += 1
                    })
                else summaryDb.insert(summary.value!!.apply {
                    todosFinished -= 1
                })
            }
        }
    }

    private fun updateDeadlineStatus(summaryDb: SummaryDao, todo: Todo) {
        scope.launch {
            withContext(Dispatchers.IO) {
                val summary = summaryDb.getSummary()
                if (todo.hasDeadline) {
                    if (todo.dateFinished!!.toLocalDate()
                            .compareTo(todo.deadlineDate!!.toLocalDate()) <= 0
                        && todo.dateFinished!!.toLocalTime()
                            .compareTo(todo.deadlineDate!!.toLocalTime()) <= 0
                    ) {
                        summaryDb.insert(summary.value!!.apply {
                            deadlinesMet += 1
                        })
                    } else {
                        summaryDb.insert(summary.value!!.apply {
                            deadlinesUnmet += 1
                        })
                    }
                }
            }
        }
    }

    private fun updateMostActive(catDb: CategoryDao, summaryDb: SummaryDao, cat: Category? = null) {
        if (cat != null) {
            scope.launch {
                withContext(Dispatchers.IO) {
                    val summary = summaryDb.getSummary()
                    val former = catDb.get(summary.value!!.mostActiveCategory)
                    if (cat.totalFinished > former?.totalFinished ?: 0) {
                        summaryDb.insert(summary.value!!.apply {
                            mostActiveCategory = cat.id
                        })
                    }
                }
            }

        } else {
            findNextMostActive(catDb, summaryDb)
        }
    }

    private fun findNextMostActive(categoryDb: CategoryDao, summaryDb: SummaryDao) {
        scope.launch {
            withContext(Dispatchers.IO) {
                val categories = categoryDb.getAll()
                var cat: Category? = null
                categories.value!!.forEach {
                    if (it.totalFinished > cat?.totalFinished ?: 0) cat = it
                }

                cat?.let {
                    withContext(Dispatchers.IO) {
                        val summary = summaryDb.getSummary()
                        summaryDb.insert(summary.value!!.apply {
                            mostActiveCategory = it.id
                        })
                    }
                }
            }
        }
    }

    private fun updateLeastActive(catDb: CategoryDao, summaryDb: SummaryDao) {

        scope.launch {
            withContext(Dispatchers.IO) {
                val summary = summaryDb.getSummary()
                val categories = catDb.getAll()
                var least: Category = categories.value!![0]
                for (i in 1..categories.value!!.size - 1) {
                    if (categories.value!![i].totalFinished < least.totalFinished)
                        least = categories.value!![i]
                }
                if (least.totalCreated > 0) {
                    val mark = with(least) { (totalFinished.toFloat() / totalCreated) * 100f }
                    if (least.id != summary.value!!.mostActiveCategory && mark < 50) {
                        withContext(Dispatchers.IO) {
                            summaryDb.insert(summary.value!!.apply {
                                leastActiveCategory = least.id
                            })
                        }
                    }
                }
            }
        }
    }

    private fun updateMostSuccessful(catDb: CategoryDao, summaryDb: SummaryDao) {
        scope.launch {
            withContext(Dispatchers.IO) {
                val summary = summaryDb.getSummary()
                val categories = catDb.getAll()
                var categoryId = summary.value!!.mostSuccessfulCategory
                var rate = summary.value!!.mostSuccessfulRatio
                categories.value!!.forEach {
                    with(it) {
                        if (totalFinished > 0
                            && Math.round((totalSuccess.toFloat() / totalFinished) * 100.0f) > rate
                        ) {
                            categoryId = it.id
                            rate = Math.round((totalSuccess.toFloat() / totalFinished) * 100)
                        }
                    }
                }

                summaryDb.insert(summary.value!!.apply {
                    mostSuccessfulRatio = rate
                    mostSuccessfulCategory = categoryId
                })
            }
        }
    }

    private fun updateLeastSuccessful(catDb: CategoryDao, summaryDb: SummaryDao) {
        scope.launch {
            withContext(Dispatchers.IO) {
                val summary = summaryDb.getSummary()
                val categories = catDb.getAll()
                var categoryId = summary.value!!.leastSuccessfulCategory
                var rate = 0
                categories.value!!.forEach {
                    with(it) {
                        if (totalFinished > 0
                            && java.lang.Math.round((totalFailure.toFloat() / totalFinished) * 100.0f) > rate
                        ) {
                            categoryId = it.id
                            rate =
                                java.lang.Math.round((totalFailure.toFloat() / totalFinished) * 100.0f)

                        }
                    }
                }

                if (rate < 50) {
                    summaryDb.insert(summary.value!!.apply {
                        leastSuccessfulCategory = categoryId
                        leastSuccessfulRatio = 100 - rate
                    })
                }
            }
        }
    }
}