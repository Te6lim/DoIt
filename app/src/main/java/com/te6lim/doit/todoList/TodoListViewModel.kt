package com.te6lim.doit.todoList

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.*
import com.te6lim.doit.broadcasts.AlarmReceiver
import com.te6lim.doit.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class TodoListViewModel(
    private val app: Application,
    private val catDb: CategoryDao, private val todoDb: TodoDbDao,
    private val summaryDb: SummaryDao
) : AndroidViewModel(app) {

    companion object {
        private var count = 0
    }

    private val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val categories = catDb.getAllLive()
    private val allTodos = todoDb.getAllLive()
    private val summary = summaryDb.getSummaryLive()

    val readySummary = fetchReadySummary()

    private var defaultCategory: Category? = null

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

    private val _lateDeadlineCount = MutableLiveData<Int>()
    val lateDeadlineCount: LiveData<Int>
        get() = _lateDeadlineCount

    private val _isTodoListEmpty = MutableLiveData<Boolean>()
    val isTodoListEmpty: LiveData<Boolean>
        get() = _isTodoListEmpty

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
            _activeCategory.value = newDefault!!
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

    @SuppressLint("UnspecifiedImmutableFlag")
    fun updateTodo(todo: Todo) {
        with(alarmManager) {
            cancel(
                PendingIntent.getBroadcast(
                    app, todo.todoId.toInt(),
                    Intent(app, AlarmReceiver::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            cancel(
                PendingIntent.getBroadcast(
                    app, Integer.MAX_VALUE - todo.todoId.toInt(),
                    Intent(app, AlarmReceiver::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            cancel(
                PendingIntent.getBroadcast(
                    app, -todo.todoId.toInt(),
                    Intent(app, AlarmReceiver::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                todoDb.update(todo.apply { isSuccess = isSuccess(this) })
                val cat = catDb.get(todo.catId)!!
                cat.apply {
                    if (todo.isFinished) totalFinished += 1
                    else totalFinished -= 1

                    if (todo.isSuccess)
                        totalSuccess += 1
                    else
                        totalFailure += 1

                    if (todo.isLate) lateTodos -= 1
                }
                catDb.update(cat)

                if (todo.isFinished) {
                    updateFinishedCount(true)
                    updateDeadlineStatus(todo)
                } else updateFinishedCount(false)

                updateMostActive(cat)
                updateLeastActive()
                updateMostSuccessful()
                updateLeastSuccessful()
            }
        }
    }

    private fun isSuccess(todo: Todo): Boolean {
        if (todo.hasDeadline) {
            with(todo) {
                return (dateFinished!!.toLocalDate() <= deadlineDate!!.toLocalDate()
                        && dateFinished!!.toLocalTime() <= deadlineDate!!.toLocalTime())
            }
        } else return todo.isFinished
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    fun delete(id: Long) {
        with(alarmManager) {
            cancel(
                PendingIntent.getBroadcast(
                    app, id.toInt(),
                    Intent(app, AlarmReceiver::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            cancel(
                PendingIntent.getBroadcast(
                    app, Integer.MAX_VALUE - id.toInt(),
                    Intent(app, AlarmReceiver::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            cancel(
                PendingIntent.getBroadcast(
                    app, -id.toInt(),
                    Intent(app, AlarmReceiver::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                todoDb.delete(id)
                //resetItemsState(todoList.value!!)
                updateDiscarded(activeCategory.value!!)
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
                selectedId.forEach { delete(it) }
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
                _selectionCount.postValue(0)
            }
        }
    }

    private fun setIsTodoEmpty(list: List<Todo>?) {
        _isTodoListEmpty.value = list?.none { !it.isFinished } ?: true
    }

    private fun setDeadlineCount(list: List<Todo>?) {
        _lateDeadlineCount.value = list?.filter { todo ->
            !todo.isFinished && todo.isLate
        }?.size ?: 0
    }

    private fun fetchList(
        inputA: LiveData<Category>, inputB: LiveData<List<Todo>?>
    ): LiveData<List<Todo>?> {
        val result = MediatorLiveData<List<Todo>?>()

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
                    resetItemsState(newList)
                    if (
                        newList.isNullOrEmpty() && count < categories.value!!.size
                        && category.totalCreated == 0
                    )
                        selectNextCategory()
                    else
                        if (category.totalCreated <= 0) result.value = null
                }
            }
        }

        val doOperationIfB = Observer<List<Todo>?> { list ->

            setIsTodoEmpty(list)
            setDeadlineCount(list)

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
        var c = 0
        var nextCategory = categories.value!![c]
        var i = 0
        while (
            i < categories.value!!.size && allTodos.value!!.none {
                !it.isFinished && it.catId == nextCategory.id
            }
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

    private fun updateFinishedCount(value: Boolean) {
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

    private fun updateDeadlineStatus(todo: Todo) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (todo.hasDeadline) {
                    if (todo.dateFinished!!.toLocalDate() <= todo.deadlineDate!!.toLocalDate()
                        && todo.dateFinished!!.toLocalTime() <= todo.deadlineDate!!.toLocalTime()
                    ) {
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

    private fun updateDiscarded(category: Category) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                summaryDb.insert(summary.value!!.apply {
                    todosDiscarded += 1
                })
                catDb.update(category.apply { totalCreated -= 1 })
            }
        }
    }

    private fun updateMostActive(cat: Category? = null) {
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

    private fun updateLeastActive() {
        var least: Category? = null
        for (i in 1 until categories.value!!.size) {
            if (least != null && categories.value!![i].totalFinished < least.totalFinished)
                least = categories.value!![i]
        }
        viewModelScope.launch {
            least?.let {
                if (it.totalCreated > 0) {
                    val mark = with(it) {
                        (totalFinished.toFloat() / totalCreated) * 100f
                    }
                    if (it.id != summary.value!!.mostActiveCategory && mark < 50) {
                        withContext(Dispatchers.IO) {
                            summaryDb.insert(summary.value!!.apply {
                                leastActiveCategory = it.id
                            })
                        }
                    }
                }
            }
        }
    }

    private fun updateMostSuccessful() {
        var categoryId = summary.value!!.mostSuccessfulCategory
        var rate = summary.value!!.mostSuccessfulRatio
        categories.value!!.forEach {
            with(it) {
                if (totalFinished > 0
                    && ((totalSuccess.toFloat() / totalFinished) * 100.0f).roundToInt() > rate
                ) {
                    categoryId = it.id
                    rate = ((totalSuccess.toFloat() / totalFinished) * 100).roundToInt()
                }
            }
        }

        if (categoryId != summary.value!!.leastActiveCategory) {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    summaryDb.insert(summary.value!!.apply {
                        mostSuccessfulRatio = rate
                        mostSuccessfulCategory = categoryId
                    })
                }
            }
        }
    }

    private fun updateLeastSuccessful() {
        var categoryId = summary.value!!.leastSuccessfulCategory
        var rate = 0
        categories.value!!.forEach {
            with(it) {
                if (totalFinished > 0
                    && ((totalFailure.toFloat() / totalFinished) * 100.0f).roundToInt() > rate
                ) {
                    categoryId = it.id
                    rate = ((totalFailure.toFloat() / totalFinished) * 100.0f).roundToInt()

                }
            }
        }

        if (categoryId != summary.value!!.mostSuccessfulCategory && rate < 50) {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    summaryDb.insert(summary.value!!.apply {
                        leastSuccessfulCategory = categoryId
                        leastSuccessfulRatio = 100 - rate
                    })
                }
            }
        }
    }

    fun getLateDeadlineCountString(): String {
        return when {
            lateDeadlineCount.value ?: 0 in 1..9 -> lateDeadlineCount.value?.toString() ?: "0"
            lateDeadlineCount.value ?: 0 > 9 -> "9+"
            else -> "${lateDeadlineCount.value ?: 0}"
        }
    }
}

class TodoListViewModelFactory(
    private val app: Application,
    private val categoryDb: CategoryDao,
    private val database: TodoDbDao,
    private val summaryDb: SummaryDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TodoListViewModel::class.java)) {
            return TodoListViewModel(app, categoryDb, database, summaryDb) as T
        }
        throw IllegalArgumentException("unknown viewModel class")
    }
}