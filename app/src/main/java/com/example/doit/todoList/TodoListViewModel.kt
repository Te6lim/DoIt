package com.example.doit.todoList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

    private val _navigatedToCreateTodo = MutableLiveData<Boolean>()
    val navigatedToCreateTodo: LiveData<Boolean>
    get() = _navigatedToCreateTodo

    init {
        _navigatedToCreateTodo.value = false
    }

    fun update() {

    }

    fun get(id: Long): Todo? {
        return null
    }

    fun update(todo: Todo) {

    }

    fun add(todoInfo: TodoInfo) {
        val todo = Todo(category = todoInfo.category)
        uiScope.launch {
            addTodo(
                todo.apply {
                    todoString = todoInfo.description
                    dateTodo = LocalDate.parse(
                        todoInfo.dateSet, DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))
                    timeTodo = LocalTime.parse(
                        todoInfo.timeSet, DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM))
                }
            )
        }
    }
    suspend fun addTodo(todo: Todo) {
        withContext(Dispatchers.IO) {
            database.insert(todo)
        }
    }

    fun delete(id: Long) {

    }

    fun navigatedToCreateTodo() {
        _navigatedToCreateTodo.value = true
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}