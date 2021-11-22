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

    private val categories = catDb.getAll()
    private val allList = todoDb.getAll()

    private val _todoList = MutableLiveData<List<Todo>>()
    val todoList: LiveData<List<Todo>>
        get() = _todoList


    private val _defaultCategory = MutableLiveData<Category?>()
    val defaultCategory: LiveData<Category?>
        get() = _defaultCategory

    private val _itemsState = MutableLiveData<MutableList<Boolean>>()
    val itemsState: LiveData<MutableList<Boolean>>
        get() = _itemsState

    private val _isNavigating = MutableLiveData<Boolean>()
    val isNavigating: LiveData<Boolean>
        get() = _isNavigating

    private val _contextActionBarEnabled = MutableLiveData(false)
    val contextActionBarEnabled: LiveData<Boolean>
        get() = _contextActionBarEnabled

    private val _viewHolderPosition = MutableLiveData<Int>()
    val viewHolderPosition: LiveData<Int>
        get() = _viewHolderPosition

    private val _selectionCount = MutableLiveData(0)
    val selectionCount: LiveData<Int>
        get() = _selectionCount

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
    }

    val defaultTransform = Transformations.map(defaultCategory) { cat ->
        allList.value?.let { list ->
            _todoList.value = filter(list, cat!!)
            resetItemsState()
        }
    }

    val isTodoListEmpty = Transformations.map(allList) { list ->
        defaultCategory.value?.let { category ->
            _todoList.value = filter(list!!, category)
        }

        list?.none { !it.isCompleted } ?: false
    }

    val itemCountInCategory = Transformations.map(todoList) { list ->
        with(defaultCategory.value) {
            if (list.isEmpty() || this == null) ("All" to todoList.value!!.size)
            else if (list[0].category != this.name) {
                ("All" to todoList.value!!.size)
            } else (name to list.size)
        }
    }

    private fun resetItemsState() {
        _itemsState.postValue(MutableList(todoList.value!!.size) { false })
    }

    var isLongPressed = false

    private fun filter(allTodos: List<Todo>, defCat: Category): List<Todo> {
        return allTodos.let { list ->
            list.filter { todo ->
                todo.category == defCat.name && !todo.isCompleted
            }.let { newList ->
                if (newList.isEmpty()) {
                    list.filter { todo ->
                        !todo.isCompleted
                    }
                } else newList
            }
        }
    }

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
                resetItemsState()
            }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                todoDb.delete(id)
                resetItemsState()
            }
        }
    }

    fun isNavigating(value: Boolean) {
        _isNavigating.value = value
    }

    fun setContextActionBarEnabled(value: Boolean) {
        if (_contextActionBarEnabled.value!! != value) _contextActionBarEnabled.value = value
    }

    fun getItems() = itemsState.value?.toList()

    fun setItemSelected(position: Int) {
        itemsState.value?.let { list ->
            list[position] = !list[position]
            if (list.any { it }) {
                setContextActionBarEnabled(true)
                if (list[position]) _selectionCount.value = _selectionCount.value!!.plus(1)
                else _selectionCount.value = _selectionCount.value!!.minus(1)
            } else setContextActionBarEnabled(false)
            _viewHolderPosition.value = position
        }
    }

    fun clickAction(position: Int = -1) {
        itemsState.value?.let { list ->
            if (_contextActionBarEnabled.value!!) {
                if (position < 0 || itemsState.value!![position]) {
                    if (position >= 0) {
                        list[position] = false
                        _viewHolderPosition.value = position
                        _selectionCount.value = _selectionCount.value!!.minus(1)
                        if (!list.any { it }) setContextActionBarEnabled(false)
                    } else {
                        for ((i, isTrue) in list.withIndex()) {
                            if (isTrue) {
                                list[i] = false
                                _viewHolderPosition.value = i
                            }
                        }
                        setContextActionBarEnabled(false)
                        _selectionCount.value = 0
                    }
                } else {
                    list[position] = true
                    _viewHolderPosition.value = position
                    _selectionCount.value = _selectionCount.value!!.plus(1)
                }
            }
        }
    }

    fun selectAll() {
        for ((i, value) in itemsState.value!!.withIndex()) {
            if (!value) {
                clickAction(i)
            }
        }
    }

    fun deleteSelected() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                for ((i, v) in itemsState.value!!.withIndex()) {
                    if (v) todoDb.delete(todoList.value!![i].todoId)
                }
                resetItemsState()
                _selectionCount.postValue(0)
            }
        }
    }

    fun updatedSelected() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                for ((i, v) in itemsState.value!!.withIndex()) {
                    if (v) todoDb.update(todoList.value!![i].apply { isCompleted = v })
                }
                resetItemsState()
                _selectionCount.postValue(0)
            }
        }
    }
}