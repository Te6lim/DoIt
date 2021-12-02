package com.example.doit.finishedTodoList

import androidx.lifecycle.*
import com.example.doit.database.Todo
import com.example.doit.database.TodoDbDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalArgumentException

class CompletedTodoListViewModel(private val todoDatabase: TodoDbDao) : ViewModel() {

    private val allTodos = todoDatabase.getAll()

    val completedTodos = Transformations.map(allTodos) {
        it?.filter { todo ->
            todo.isFinished
        }?.sortedBy { t -> t.dateFinished }?.reversed()
    }

    fun clearFinished() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                allTodos.value?.let {
                    it.filter { todo ->
                        todo.isFinished
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

class FinishedTodoListViewModelFactory(private val todoDatabase: TodoDbDao) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CompletedTodoListViewModel::class.java)) {
            return CompletedTodoListViewModel(todoDatabase) as T
        }
        throw IllegalArgumentException("unknown view model class")
    }
}