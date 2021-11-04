package com.example.doit.todoList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.doit.database.Category
import com.example.doit.database.CategoryDao
import com.example.doit.database.Todo
import com.example.doit.database.TodoDbDao
import com.example.doit.todoList.createTodo.CreateTodoViewModel
import kotlinx.coroutines.*

class TodoListViewModel(
    private val catDb: CategoryDao, private val todoDb: TodoDbDao
) : ViewModel() {

    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val allList = todoDb.getAll()

    private val _todoList = MutableLiveData<List<Todo>>()
    val todoList: LiveData<List<Todo>>
        get() = _todoList

    private val categories = catDb.getAll()

    private val _defaultCategory = MutableLiveData<Category>()
    val defaultCategory: LiveData<Category>
        get() = _defaultCategory

    val catTrans = Transformations.map(categories) { catList ->
        if (catList.isNullOrEmpty()) {
            val cat = Category(
                name = CreateTodoViewModel.DEFAULT_CATEGORY, isDefault = true
            )
            uiScope.launch {
                withContext(Dispatchers.IO) {
                    catDb.insert(cat)
                }
            }
        }
        emitDefault()
    }

    val defTrans = Transformations.map(defaultCategory) {
        _todoList.value = allList.value
    }

    val isTodoListEmpty = Transformations.map(allList) {
        if (_todoList.value != null) _todoList.value = allList.value
        it?.isEmpty() ?: false
    }

    val todoListByCategory = Transformations.map(todoList) {
        it?.let { list ->
            list.filter { todo ->
                todo.category == defaultCategory.value?.name
            }.let { newList ->
                if (newList.isEmpty()) list
                else newList
            }
        }
    }

    val itemCountInCategory = Transformations.map(todoListByCategory) { list ->
        with(defaultCategory.value) {
            if (this == null) ("All" to todoList.value!!.size)
            else (name to list.size)
        }
    }

    private fun emitDefault() {
        uiScope.launch {
            _defaultCategory.value = getDefault()
        }
    }

    private suspend fun getDefault(): Category {
        return withContext(Dispatchers.IO) {
            catDb.getDefault()
        }
    }

    /*fun changeDefault(categoryId: Int) {
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
    }*/

    fun delete(id: Long) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                todoDb.delete(id)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}