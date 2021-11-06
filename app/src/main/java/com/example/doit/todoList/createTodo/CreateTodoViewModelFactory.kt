package com.example.doit.todoList.createTodo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.doit.database.CategoryDao
import com.example.doit.database.TodoDbDao

class CreateTodoViewModelFactory(
    private val todoDb: TodoDbDao, private val catDb: CategoryDao,
    private val defaultCategoryId: Int
) :
    ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateTodoViewModel::class.java)) {
            return CreateTodoViewModel(todoDb, catDb, defaultCategoryId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}