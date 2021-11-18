package com.example.doit.completedTodoList

import androidx.lifecycle.*
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

    val subtitleText = Transformations.map(completedTodos) {
        it.size
    }

    fun clearFinished() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                allTodos.value?.let {
                    it.filter { todo ->
                        todo.isCompleted
                    }.let { list ->
                        list.forEach { todo ->
                            todoDatabase.delete(todo.todoId)
                        }
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