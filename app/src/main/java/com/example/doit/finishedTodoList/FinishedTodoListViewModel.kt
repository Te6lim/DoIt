package com.example.doit.finishedTodoList

import androidx.lifecycle.*
import com.example.doit.database.Category
import com.example.doit.database.CategoryDao
import com.example.doit.database.Todo
import com.example.doit.database.TodoDbDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalArgumentException

class FinishedTodoListViewModel(
    private val categoryDb: CategoryDao, private val todoDatabase: TodoDbDao
) : ViewModel() {

    private val _defaultCategory = MutableLiveData<Category>()
    private val defaultCategory: LiveData<Category>
        get() = _defaultCategory

    private val allTodos = todoDatabase.getAll()


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
                _defaultCategory.postValue(categoryDb.getDefault())
            }
        }
    }

    fun emitCategory(id: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _defaultCategory.postValue(categoryDb.get(id))
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

    fun updateTodo(catId: Int, todo: Todo) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                todoDatabase.update(todo)
                categoryDb.update(
                    categoryDb.get(catId)!!.apply {
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
}

class FinishedTodoListViewModelFactory(
    private val categoryDb: CategoryDao,
    private val todoDatabase: TodoDbDao
) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinishedTodoListViewModel::class.java)) {
            return FinishedTodoListViewModel(categoryDb, todoDatabase) as T
        }
        throw IllegalArgumentException("unknown view model class")
    }
}