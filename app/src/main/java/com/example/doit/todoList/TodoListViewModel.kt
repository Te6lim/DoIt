package com.example.doit.todoList

import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.example.doit.database.*
import kotlinx.coroutines.*
import java.lang.IllegalArgumentException

class TodoListViewModel(
    private val catDb: CategoryDao, private val todoDb: TodoDbDao,
    private val summaryDb: SummaryDao
) : ViewModel() {

    companion object {
        var count = 0
    }

    val categories = catDb.getAll()
    val allTodos = todoDb.getAll()
    private val summary = summaryDb.getSummary()

    val readySummary = fetchReadySummary()

    var defaultCategory: Category? = null
        private set

    private val _activeCategory = MutableLiveData<Category>()
    val activeCategory: LiveData<Category>
        get() = _activeCategory

    private var listIsReady: Boolean = true

    private var itemsState = mutableListOf<Boolean>()

    private val _isNavigating = MutableLiveData<Boolean>()
    val isNavigating: LiveData<Boolean>
        get() = _isNavigating

    private val _selectionCount = MutableLiveData(0)
    val selectionCount: LiveData<Int>
        get() = _selectionCount

    val categoriesTransform = Transformations.map(categories) { catList ->
        count = 0
        if (catList.isNullOrEmpty()) {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    catDb.insert(Category())
                }
            }
        }
        emitDefault()
    }

    val todoList = fetchList(activeCategory, allTodos)

    val isTodoListEmpty = Transformations.map(allTodos) { list ->
        list?.let { resetItemsState(it) }
        list?.none { !it.isFinished } ?: true
    }

    private val _itemCountInCategory = MutableLiveData<Pair<String, Int>>()
    val itemCountInCategory: LiveData<Pair<String, Int>>
    get() = _itemCountInCategory

    fun itemsState() = itemsState.toList()

    private fun resetItemsState(list: List<Todo>) {
        itemsState = MutableList(list.size) { false }
    }

    private val _isLongPressed = MutableLiveData(false)
    val isLongPressed: LiveData<Boolean>
        get() = _isLongPressed

    var longPressStatusChanged = false
        private set

    var editTodo: Todo? = null
        private set

    private fun filter(allTodos: List<Todo>, defCat: Category?): List<Todo> {
        return allTodos.let { list ->
            list.filter { todo ->
                if (defCat != null) todo.catId == defCat.id && !todo.isFinished
                else true
            }
        }
    }

    private fun emitDefault() {
        val newDefault = categories.value!!.find { it.isDefault }
        if (newDefault != defaultCategory) {
            _activeCategory.value = newDefault
            defaultCategory = _activeCategory.value
        }
    }

    fun emitAsActive(id: Int, isListAvailable: Boolean) {
        listIsReady = isListAvailable
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
                todoDb.update(todo.apply { isSuccess = isSuccess(this) })
                resetItemsState(todoList.value!!)
                val cat = catDb.get(todo.catId)!!.apply {
                    if (todo.isFinished) totalFinished += 1
                    else totalFinished -= 1

                    if (todo.isSuccess) totalSuccess += 1
                    else totalFailure += 1
                }
                catDb.update(cat)

                if (todo.isFinished) {
                    updateFinishedCount(true)
                    updateDeadlineStatus(todo)
                } else updateFinishedCount(false)

                updateMostActive(cat)
                updateLeastActive()
                updateMostSuccessful()
            }
        }
    }

    private fun isSuccess(todo: Todo): Boolean {
        if (todo.hasDeadline) {
            with(todo) {
                if (
                    dateFinished!!.toLocalDate() <= deadlineDate!!.toLocalDate()
                    && dateFinished!!.toLocalTime() <= deadlineDate!!.toLocalTime()
                ) return true
                else return false
            }
        } else {
            with(todo) {
                if (dateFinished!!.toLocalDate() > dateTodo.toLocalDate()) return true
                else return false
            }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                todoDb.delete(id)
                resetItemsState(todoList.value!!)
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
        resetItemsState(todoList.value!!)
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
                resetItemsState(todoList.value!!)
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
                resetItemsState(todoList.value!!)
                _selectionCount.postValue(0)
            }
        }
    }

    private fun fetchList(
        inputA: LiveData<Category>, inputB: LiveData<List<Todo>?>
    ): LiveData<List<Todo>> {
        val result = MediatorLiveData<List<Todo>>()

        val doOperationIfA = Observer<Category> { category ->
            inputB.value?.let { list ->
                val newList = filter(list, category).sortedBy { !it.hasDeadline }
                if (!newList.isNullOrEmpty() && listIsReady) {
                    if (newList.isEmpty() && category != defaultCategory)
                        selectNextCategory()
                    else {
                        _itemCountInCategory.value = category.name to newList.size
                        resetItemsState(newList)
                        result.value = newList
                    }
                } else {
                    _itemCountInCategory.value = category.name to 0
                    result.value = null
                }
            }
        }

        val doOperationIfB = Observer<List<Todo>?> { list ->
            listIsReady = true
            inputA.value?.let { category ->
                count = 0
                val newList = filter(list, category).sortedBy { !it.hasDeadline }
                if (newList.isEmpty())
                    selectNextCategory()
                else {
                    _itemCountInCategory.value = category.name to newList.size
                    resetItemsState(newList)
                    result.value = newList
                }
            }
        }

        result.addSource(inputA, doOperationIfA)

        result.addSource(inputB, doOperationIfB)
        return result
    }

    private fun selectNextCategory() {
        var c: Int
        if (count == 0 && activeCategory.value!! == defaultCategory)
            c = ++count % categories.value!!.size
        else c = count++ % categories.value!!.size
        var nextCategory = categories.value!![c]
        var i = 0
        while (
            i < categories.value!!.size - 1 && allTodos.value!!.none { it.catId == nextCategory.id }
        ) {
            c = ++count % categories.value!!.size
            nextCategory = categories.value!![c]
            ++i
        }
        _activeCategory.value = nextCategory
    }

    private fun fetchReadySummary(): LiveData<Summary> {
        val result = MediatorLiveData<Summary>()
        val action = Observer<Summary?> {
            if (it == null) {
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        summaryDb.insert(Summary())
                    }
                }
            } else {
                result.value = it
            }
        }
        result.addSource(summary, action)
        return result
    }

    fun updateFinishedCount(value: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (value)
                    summaryDb.insert(summary.value!!.apply {
                        todosFinished += 1
                    })
                else summaryDb.insert(summary.value!!.apply {
                    todosFinished -= 1
                })
            }
        }
    }

    fun updateDeadlineStatus(todo: Todo) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (todo.hasDeadline) {
                    if (todo.dateFinished!!.toLocalDate() <= todo.deadlineDate!!.toLocalDate()) {
                        summaryDb.insert(summary.value!!.apply {
                            deadlinesMet += 1
                        })
                    } else {
                        summaryDb.insert(summary.value!!.apply {
                            deadlinesUnmet += 1
                        })
                    }
                }
            }
        }
    }

    fun updateDiscarded() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                summaryDb.insert(summary.value!!.apply {
                    todosDiscarded += 1
                })
            }
        }
    }

    fun updateMostActive(cat: Category? = null) {
        if (cat != null) {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    val former = catDb.get(summary.value!!.mostActiveCategory)
                    if (cat.totalFinished > former?.totalFinished ?: 0) {
                        summaryDb.insert(summary.value!!.apply {
                            mostActiveCategory = cat.id
                        })
                    }
                }
            }

        } else {
            findNextMostActive()
        }
    }

    private fun findNextMostActive() {
        var cat: Category? = null
        categories.value!!.forEach {
            if (it.totalFinished > cat?.totalFinished ?: 0) cat = it
        }

        cat?.let {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    summaryDb.insert(summary.value!!.apply {
                        mostActiveCategory = it.id
                    })
                }
            }
        }
    }

    fun updateLeastActive() {
        var least: Category = categories.value!![0]
        for (i in 1..categories.value!!.size - 1) {
            if (categories.value!![i].totalFinished < least.totalFinished)
                least = categories.value!![i]
        }
        viewModelScope.launch {
            if (least.id != summary.value!!.mostActiveCategory) {
                withContext(Dispatchers.IO) {
                    summaryDb.insert(summary.value!!.apply {
                        leastActiveCategory = least.id
                    })
                }
            } else {
                withContext(Dispatchers.IO) {
                    summaryDb.insert(summary.value!!.apply {
                        leastActiveCategory = -1
                    })
                }
            }
        }
    }

    fun updateMostSuccessful() {
        var category = -1
        var rate = 0L
        categories.value!!.forEach {
            with(it) {
                if (totalFinished > 0 && Math.round((totalSuccess / totalFinished) * 100.0) > rate) {
                    rate = Math.round((totalSuccess / totalFinished) * 100.0)
                    category = it.id
                }
            }
        }

        if (category != -1) {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    summaryDb.insert(summary.value!!.apply {
                        mostSuccessfulCategory = category
                        mostSuccessfulRatio = rate.toInt()
                    })
                }
            }
        }
    }
}

class TodoListViewModelFactory(
    private val categoryDb: CategoryDao,
    private val database: TodoDbDao,
    private val summaryDb: SummaryDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TodoListViewModel::class.java)) {
            return TodoListViewModel(categoryDb, database, summaryDb) as T
        }
        throw IllegalArgumentException("unknown viewModel class")
    }
}