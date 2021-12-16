package com.example.doit.finishedTodoList

import androidx.lifecycle.*
import com.example.doit.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalArgumentException

class FinishedTodoListViewModel(
    private val catDb: CategoryDao, private val todoDatabase: TodoDbDao,
    private val summaryDb: SummaryDao
) : ViewModel() {

    private val _defaultCategory = MutableLiveData<Category>()
    private val defaultCategory: LiveData<Category>
        get() = _defaultCategory

    private val allTodos = todoDatabase.getAll()
    private val summary = summaryDb.getSummary()

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

    fun clearFinishedTodos() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                allTodos.value?.let {
                    it.filter { todo ->
                        todo.isFinished && todo.catId == defaultCategory.value!!.id
                    }.let { list ->
                        list.forEach { todo ->
                            todoDatabase.delete(todo.todoId)
                        }
                    }
                }
            }
        }
    }

    fun updateTodo(todo: Todo) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                todoDatabase.update(todo)
            }
        }
    }

    fun updateCategoryFinished(todo: Todo) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                catDb.update(
                    catDb.get(todo.catId)!!.apply {
                        if (todo.isFinished) totalFinished += 1
                        else totalFinished -= 1
                    }
                )
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
}

class FinishedTodoListViewModelFactory(
    private val categoryDb: CategoryDao,
    private val todoDatabase: TodoDbDao,
    private val summaryDb: SummaryDao
) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinishedTodoListViewModel::class.java)) {
            return FinishedTodoListViewModel(categoryDb, todoDatabase, summaryDb) as T
        }
        throw IllegalArgumentException("unknown view model class")
    }
}