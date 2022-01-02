package com.te6lim.doit.finishedTodoList

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.*
import com.te6lim.doit.broadcasts.AlarmReceiver
import com.te6lim.doit.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FinishedTodoListViewModel(
    private val app: Application,
    private val catDb: CategoryDao, private val todoDb: TodoDbDao,
    private val summaryDb: SummaryDao
) : ViewModel() {

    private val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private val _defaultCategory = MutableLiveData<Category>()
    private val defaultCategory: LiveData<Category>
        get() = _defaultCategory

    private val allTodos = todoDb.getAllLive()
    private val summary = summaryDb.getSummaryLive()

    val readySummary = fetchReadySummary()

    init {
        initializeCategory()
    }

    val completedTodos = fetchList(defaultCategory, allTodos)

    val categoryCountPair = Transformations.map(completedTodos) {
        Pair(defaultCategory.value!!.name, it?.size ?: 0)
    }

    private val _navigating = MutableLiveData<Boolean>()
    val navigating: LiveData<Boolean>
        get() = _navigating

    private fun initializeCategory() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _defaultCategory.postValue(catDb.getDefault())
            }
        }
    }

    fun emitCategory(id: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _defaultCategory.postValue(catDb.get(id))
            }
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    fun delete(id: Long) {
        with(alarmManager) {
            cancel(
                android.app.PendingIntent.getBroadcast(
                    app, id.toInt(),
                    Intent(app, AlarmReceiver::class.java),
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            cancel(
                android.app.PendingIntent.getBroadcast(
                    app, Integer.MAX_VALUE - id.toInt(),
                    Intent(app, AlarmReceiver::class.java),
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                todoDb.delete(id)
            }
        }
    }

    fun clearFinishedTodos() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                allTodos.value?.let {
                    it.filter { todo ->
                        todo.isFinished && todo.catId == defaultCategory.value!!.id
                    }.let { list ->
                        list.forEach { todo ->
                            delete(todo.todoId)
                        }
                    }
                }
            }
        }
    }

    fun isNavigating(value: Boolean) {
        _navigating.value = value
    }

    private fun fetchList(
        category: LiveData<Category>, list: LiveData<List<Todo>?>
    ): LiveData<List<Todo>> {
        val result = MediatorLiveData<List<Todo>>()

        val operationOnCategoryReady = Observer<Category> { cat ->
            result.value = list.value?.filter { todo ->
                todo.isFinished && todo.catId == cat.id
            }?.sortedBy { t -> t.dateFinished }?.reversed()
        }

        val operationOnListReady = Observer<List<Todo>?> {
            result.value = it.filter { todo ->
                todo.isFinished && todo.catId == category.value!!.id
            }.sortedBy { t -> t.dateFinished }.reversed()
        }

        result.addSource(category, operationOnCategoryReady)
        result.addSource(list, operationOnListReady)

        return result
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
}

class FinishedTodoListViewModelFactory(
    private val app: Application,
    private val categoryDb: CategoryDao,
    private val todoDatabase: TodoDbDao,
    private val summaryDb: SummaryDao
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinishedTodoListViewModel::class.java)) {
            return FinishedTodoListViewModel(app, categoryDb, todoDatabase, summaryDb) as T
        }
        throw IllegalArgumentException("unknown view model class")
    }
}