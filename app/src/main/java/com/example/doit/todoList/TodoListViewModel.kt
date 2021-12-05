package com.example.doit.todoList

import androidx.lifecycle.*
import com.example.doit.database.Category
import com.example.doit.database.CategoryDao
import com.example.doit.database.Todo
import com.example.doit.database.TodoDbDao
import com.example.doit.todoList.createTodo.CreateTodoViewModel
import kotlinx.coroutines.*
import java.lang.IllegalArgumentException

class TodoListViewModel(
    private val catDb: CategoryDao, private val todoDb: TodoDbDao
) : ViewModel() {

    private val categories = catDb.getAll()
    private val allTodos = todoDb.getAll()

    private val _todoList = MutableLiveData<List<Todo>>()
    val todoList: LiveData<List<Todo>>
        get() = _todoList

    private var defaultCategory: Category? = null

    private val _activeCategory = MutableLiveData<Category>()
    val activeCategory: LiveData<Category>
        get() = _activeCategory

    private var itemsState = mutableListOf<Boolean>()

    private val _isNavigating = MutableLiveData<Boolean>()
    val isNavigating: LiveData<Boolean>
        get() = _isNavigating

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

        emitDefault()
    }

    val defaultTransform = Transformations.map(activeCategory) { cat ->
        allTodos.value?.let { list ->
            _todoList.value = filter(list, cat!!).sortedBy { !it.hasDeadline }
            resetItemsState()
        }
    }

    val isTodoListEmpty = Transformations.map(allTodos) { list ->
        activeCategory.value?.let { category ->
            val newList = filter(list!!, category).sortedBy { !it.hasDeadline }
            if (newList.isEmpty())
                _activeCategory.value = defaultCategory!!
            else _todoList.value = newList
            resetItemsState()
        }

        list?.none { !it.isFinished } ?: false
    }

    val itemCountInCategory = Transformations.map(todoList) { list ->
        with(activeCategory.value) {
            this!!.name to list.size
        }
    }

    fun itemsState() = itemsState.toList()

    private fun resetItemsState() {
        itemsState = MutableList(todoList.value!!.size) { false }
    }

    private val _isLongPressed = MutableLiveData(false)
    val isLongPressed: LiveData<Boolean>
        get() = _isLongPressed

    var longPressStatusChanged = false
        private set

    var editTodo: Todo? = null
        private set

    private fun filter(allTodos: List<Todo>, defCat: Category): List<Todo> {
        return allTodos.let { list ->
            list.filter { todo ->
                todo.catId == defCat.id && !todo.isFinished
            }
        }
    }

    private fun emitDefault() {
        viewModelScope.launch {
            val newDefault = getDefault()
            if (newDefault != defaultCategory) {
                _activeCategory.value = newDefault
                defaultCategory = _activeCategory.value
            }
        }
    }

    fun emitAsActive(id: Int) {
        viewModelScope.launch {
            _activeCategory.value = getCategoryById(id)
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

    fun interact(position: Int = -1, isLongPress: Boolean = false): Boolean {
        if (isLongPress) {
            if (!_isLongPressed.value!!) {
                itemsState = mutableListOf()
                todoList.value!!.forEach { _ -> itemsState.add(false) }
                _isLongPressed.value = true
                longPressStatusChanged = true
            }
            return actionOnLongPressActive(position)
        } else {
            if (_isLongPressed.value!!) return actionOnLongPressActive(position)
        }
        return false
    }

    private fun actionOnLongPressActive(position: Int): Boolean {
        if (position == -1) {
            resetStateValues()
            return false
        }

        itemsState[position] = !itemsState[position]
        if (itemsState[position]) {
            _selectionCount.value = _selectionCount.value?.plus(1)
        } else {
            _selectionCount.value = _selectionCount.value?.minus(1)
            if (_selectionCount.value == 0) resetStateValues()
        }

        if (selectionCount.value == 1) editTodo = todoList.value!![position]

        return itemsState[position]
    }

    private fun resetStateValues() {
        _selectionCount.value = 0
        _isLongPressed.value = false
        longPressStatusChanged = true
        editTodo = null
        resetItemsState()
    }

    fun setLongPressedStatusChanged(value: Boolean) {
        longPressStatusChanged = value
    }

    fun deleteSelected() {
        viewModelScope.launch {
            val selectedId = mutableListOf<Long>()
            for ((i, v) in itemsState.withIndex())
                if (v) selectedId.add(todoList.value!![i].todoId)

            withContext(Dispatchers.IO) {
                selectedId.forEach { todoDb.delete(it) }
                resetItemsState()
                _selectionCount.postValue(0)
            }
        }
    }

    fun selectAll(value: Boolean) {
        if (_isLongPressed.value!!) {
            for ((i, _) in itemsState.withIndex()) {
                itemsState[i] = value
            }
            if (value) _selectionCount.value = itemsState.size
            else _selectionCount.value = 0
        }
    }

    fun updatedSelected() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                for ((i, v) in itemsState.withIndex()) {
                    if (v) todoDb.update(todoList.value!![i].apply { isFinished = v })
                }
                resetItemsState()
                _selectionCount.postValue(0)
            }
        }
    }
}

class TodoListViewModelFactory(
    private val categoryDb: CategoryDao,
    private val database: TodoDbDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TodoListViewModel::class.java)) {
            return TodoListViewModel(categoryDb, database) as T
        }
        throw IllegalArgumentException("unknown viewModel class")
    }
}