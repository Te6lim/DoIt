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

    init {
        initializeCategory()
    }

    private val allTodos = todoDatabase.getAll()

    val awaitCategory = Transformations.map(defaultCategory) {
        allTodos.value?.let { list ->
            _completedTodos.value = list.filter { todo ->
                todo.isFinished && todo.catId == it.id
            }.sortedBy { t -> t.dateFinished }.reversed()
        }
    }

    val awaitTodoList = Transformations.map(allTodos) {
        it?.let { list ->
            _completedTodos.value = list.filter { todo ->
                todo.isFinished && todo.catId == defaultCategory.value!!.id
            }.sortedBy { t -> t.dateFinished }.reversed()
        }
    }

    private val _completedTodos = MutableLiveData<List<Todo>>()
    val completedTodos: LiveData<List<Todo>>
        get() = _completedTodos

    val subtitleData = Transformations.map(completedTodos) {
        Pair(defaultCategory.value!!.name, it.size)
    }

    private fun initializeCategory() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _defaultCategory.postValue(categoryDb.getDefault())
            }
        }
    }

    fun clearFinished() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                allTodos.value?.let {
                    it.filter { todo ->
                        todo.isFinished
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