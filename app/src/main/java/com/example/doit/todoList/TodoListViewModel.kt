package com.example.doit.todoList

import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.doit.database.Todo
import com.example.doit.database.TodoDbDao
import com.example.doit.todoList.createTodo.TodoInfo
import kotlinx.coroutines.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class TodoListViewModel(private val database: TodoDbDao) : ViewModel() {

    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val todoList = database.getAll()

    val isTodoListEmpty = Transformations.map(todoList) {
        it?.isEmpty() ?: false
    }

    fun get(id: Long): Todo? {
        return null
    }

    fun update(todo: Todo) {

    }

    fun delete(id: Long) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                database.delete(id)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}