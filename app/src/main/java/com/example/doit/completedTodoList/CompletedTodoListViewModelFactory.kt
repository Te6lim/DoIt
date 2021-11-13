package com.example.doit.completedTodoList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.doit.database.TodoDbDao
import java.lang.IllegalArgumentException

class CompletedTodoListViewModelFactory(private val todoDatabase: TodoDbDao) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CompletedTodoListViewModel::class.java)) {
            return CompletedTodoListViewModel(todoDatabase) as T
        }
        throw IllegalArgumentException("unknown view model class")
    }
}