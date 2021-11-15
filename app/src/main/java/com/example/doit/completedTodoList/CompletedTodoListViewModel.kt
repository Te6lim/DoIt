package com.example.doit.completedTodoList

import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doit.database.Todo
import com.example.doit.database.TodoDbDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CompletedTodoListViewModel(private val todoDatabase: TodoDbDao) : ViewModel() {

    private val allTodos = todoDatabase.getAll()

    val completedTodos = Transformations.map(allTodos) {
        it?.let { list ->
            list.filter { todo ->
                todo.isCompleted
            }
        }
    }

    fun clearFinished() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                allTodos.value?.let { list ->
                    for (item in list) {
                        if (item.isCompleted) todoDatabase.delete(item.todoId)
                    }
                }
            }
        }
    }

    fun updateTodo(todo: Todo) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                todoDatabase.update(todo)
            }
        }
    }
}