package com.example.doit.todoList.createTodo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.doit.database.CategoryDao

class CreateTodoViewModelFactory(private val db: CategoryDao) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateTodoViewModel::class.java)) {
            return CreateTodoViewModel(db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}