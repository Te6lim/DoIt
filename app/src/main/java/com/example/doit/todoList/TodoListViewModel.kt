package com.example.doit.todoList

import androidx.lifecycle.*
import com.example.doit.database.Category
import com.example.doit.database.CategoryDao
import com.example.doit.database.Todo
import com.example.doit.database.TodoDbDao
import com.example.doit.todoList.createTodo.CreateTodoViewModel
import kotlinx.coroutines.*

class TodoListViewModel(
    private val catDb: CategoryDao, private val todoDb: TodoDbDao
) : ViewModel() {

    private val allList = todoDb.getAll()

    private val _todoList = MutableLiveData<List<Todo>>()
    val todoList: LiveData<List<Todo>>
        get() = _todoList

    val categories = catDb.getAll()

    private val _defaultCategory = MutableLiveData<Category?>()
    val defaultCategory: LiveData<Category?>
        get() = _defaultCategory

    val categoriesTransform = Transformations.map(categories) { catList ->
        if (catList.isNullOrEmpty()) {
            val cat = Category(
                name = CreateTodoViewModel.DEFAULT_CATEGORY, isDefault = true
            )
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    catDb.insert(cat)
                }
            }
        }
        if (_defaultCategory.value == null) emitDefault()
        catList.toListOfString { it.name }
    }

    private fun List<Category>?.toListOfString(f: (Category) -> String): List<String> {
        val list = mutableListOf<String>()
        this?.let {
            for (item in this) {
                list.add(f(item))
            }
        }
        return list
    }

    val defaultTransform = Transformations.map(defaultCategory) {
        _todoList.value = allList.value

    }

    val isTodoListEmpty = Transformations.map(allList) {
        if (_todoList.value != null) {
            _todoList.value = allList.value
        }
        it?.isEmpty() ?: false
    }

    val todoListByCategory = Transformations.map(todoList) {
        it?.let { list ->
            list.filter { todo ->
                todo.category == defaultCategory.value?.name && !todo.isCompleted
            }.let { newList ->
                if (newList.isEmpty()) {
                    list
                } else newList
            }
        }
    }

    val itemCountInCategory = Transformations.map(todoListByCategory) { list ->
        with(defaultCategory.value) {
            if (list.isEmpty() || this == null) ("All" to todoList.value!!.size)
            else if (list[0].category != this.name) {
                ("All" to todoList.value!!.size)
            } else (name to list.size)
        }
    }

    private val _isNavigating = MutableLiveData<Boolean>()
    val isNavigating: LiveData<Boolean>
        get() = _isNavigating

    private val _contextActionBarEnabled = MutableLiveData<Boolean>()
    val contextActionBarEnabled: LiveData<Boolean>
        get() = _contextActionBarEnabled

    private fun emitDefault() {
        viewModelScope.launch {
            _defaultCategory.value = getDefault()
        }
    }

    fun emitDisplayCategoryAsDefault(id: Int) {
        if (defaultCategory.value != null) {
            viewModelScope.launch {
                _defaultCategory.value = getCategoryById(id)
            }
        }
    }

    private suspend fun getCategoryById(id: Int): Category? {
        return withContext(Dispatchers.IO) {
            catDb.get(id)
        }
    }

    private suspend fun getDefault(): Category {
        return withContext(Dispatchers.IO) {
            catDb.getDefault()
        }
    }

    fun updateTodo(todo: Todo) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                todoDb.update(todo)
            }
        }
    }

    private suspend fun getTodo(id: Long): Todo? {
        return withContext(Dispatchers.IO) {
            todoDb.get(id)
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
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                todoDb.delete(id)
            }
        }
    }

    fun isNavigating(value: Boolean) {
        _isNavigating.value = value
    }

    fun contextActionBarEnabled(value: Boolean) {
        _contextActionBarEnabled.value = value
    }
}