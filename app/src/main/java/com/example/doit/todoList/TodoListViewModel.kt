package com.example.doit.todoList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.doit.database.Category
import com.example.doit.database.CategoryDao
import com.example.doit.database.TodoDbDao
import com.example.doit.todoList.createTodo.CreateTodoViewModel
import kotlinx.coroutines.*

class TodoListViewModel(
    private val catDb: CategoryDao, private val database: TodoDbDao
) : ViewModel() {

    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val todoList = database.getAll()
    val categories = catDb.getAll()

    val isTodoListEmpty = Transformations.map(todoList) {
        it?.isEmpty() ?: false
    }


    private val _defaultCategory = MutableLiveData<Category>()
    val defaultCategory: LiveData<Category>
        get() = _defaultCategory

    fun initializeCategories() {
        addNewCategory(CreateTodoViewModel.DEFAULT_CATEGORY, true)
        addNewCategory("School")
    }

    private fun addNewCategory(newCategory: String, default: Boolean = false) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                catDb.insert(Category(name = newCategory, isDefault = default))
            }
        }
    }

    fun initializeDefault() {
        uiScope.launch {
            _defaultCategory.value = defaultCategory()
        }
    }

    private suspend fun defaultCategory(): Category {
        return withContext(Dispatchers.IO) {
            val default = catDb.getDefault(true)
            default
        }
    }

    fun changeDefault(categoryId: Int) {
        _defaultCategory.value?.let {
            it.isDefault = false
            var newDefault: Category?
            uiScope.launch {
                withContext(Dispatchers.IO) {
                    newDefault = catDb.get(categoryId)
                }
                newDefault?.isDefault = true
                _defaultCategory.value = newDefault ?: it.apply { isDefault = true }
            }
        }
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