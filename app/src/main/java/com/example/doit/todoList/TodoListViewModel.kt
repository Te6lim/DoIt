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

    private val categories = catDb.getAll()

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
                    list.filter { todo ->
                        !todo.isCompleted
                    }
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

    val items = Transformations.map(allList) {
        val list = mutableListOf<Boolean>()
        it?.let {
            it.forEach { _ -> list.add(false) }
        }
        list
    }

    private val _isNavigating = MutableLiveData<Boolean>()
    val isNavigating: LiveData<Boolean>
        get() = _isNavigating

    private val _contextActionBarEnabled = MutableLiveData(false)
    val contextActionBarEnabled: LiveData<Boolean>
        get() = _contextActionBarEnabled

    private val _viewHolderPosition = MutableLiveData<Int>()
    val viewHolderPosition: LiveData<Int>
        get() = _viewHolderPosition

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

    fun setContextActionBarEnabled(value: Boolean) {
        if (_contextActionBarEnabled.value!! != value) _contextActionBarEnabled.value = value
    }

    fun getItems() = items.value?.toList()

    fun setItemSelected(position: Int) {
        items.value?.let { list ->
            list[position] = !list[position]
            if (list.any { it }) setContextActionBarEnabled(true)
            else setContextActionBarEnabled(false)
            _viewHolderPosition.value = position
        }
    }

    fun clickAction(position: Int = -1) {
        items.value?.let { list ->
            if (_contextActionBarEnabled.value!!) {
                if (position < 0 || items.value!![position]) {
                    if (position >= 0) {
                        list[position] = false
                        _viewHolderPosition.value = position
                        if (!list.any { it }) setContextActionBarEnabled(false)
                    } else {
                        (items as MutableLiveData).value = MutableList(list.size) { false }
                    }
                } else {
                    list[position] = true
                    _viewHolderPosition.value = position
                }
            }
        }
    }
}