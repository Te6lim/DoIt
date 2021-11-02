package com.example.doit.todoList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.doit.database.CategoryDao
import com.example.doit.database.TodoDbDao
import java.lang.IllegalArgumentException

class TodoListViewModelFactory(
    private val categoryDb: CategoryDao,
    private val database: TodoDbDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TodoListViewModel::class.java)) {
            return TodoListViewModel(categoryDb, database) as T
        }
        throw IllegalArgumentException("unknown viewModel class")
    }
}