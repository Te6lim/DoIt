package com.example.doit.todoList.createTodo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.doit.database.Category
import com.example.doit.database.CategoryDao
import com.example.doit.database.Todo
import com.example.doit.database.TodoDbDao
import kotlinx.coroutines.*

class CreateTodoViewModel(
    private val todoDb: TodoDbDao, private val catDb: CategoryDao, defaultCategoryId: Int
) : ViewModel() {

    companion object {
        const val DEFAULT_CATEGORY = "Work"
    }

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val categories = catDb.getAll()

    private val _todoInfo = MutableLiveData<TodoInfo>()
    val todoInfo: LiveData<TodoInfo>
        get() = _todoInfo

    var todo = TodoInfo()
        private set

    private val _categoryEditTextIsOpen = MutableLiveData<Boolean>()
    val categoryEditTextIsOpen: LiveData<Boolean>
        get() = _categoryEditTextIsOpen

    private val _category = MutableLiveData<Category>()
    val category: LiveData<Category>
        get() = _category

    init {
        emitCategory(defaultCategoryId)
    }

    fun add(todoInfo: TodoInfo) {
        uiScope.launch {
            val todo = Todo(
                todoString = todoInfo.description,
                category = todoInfo.category,
                dateTodo = todoInfo.dateSet,
                timeTodo = todoInfo.timeSet,
                hasDeadline = todoInfo.deadlineEnabled,
            ).apply {
                if (hasDeadline) {
                    deadlineDate = todoInfo.deadlineDate
                    deadlineTime = todoInfo.deadlineTime
                }
            }
            addTodo(todo)
        }
    }

    private suspend fun addTodo(todo: Todo) {
        withContext(Dispatchers.IO) {
            todoDb.insert(todo)
        }
    }

    fun createTodoInfo() {
        _todoInfo.value = todo
    }

    fun clearTodoInfo() {
        todo = TodoInfo()
        _todoInfo.value = todo
    }

    fun addNewCategory(newCategory: String, default: Boolean = false) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                catDb.insert(Category(name = newCategory,isDefault = default))
            }
        }
    }

    fun emitCategory(id: Int) {
        uiScope.launch {
            _category.value = getCat(id)
        }
    }

    private suspend fun getCat(id: Int): Category? {
        return withContext(Dispatchers.IO) {
            catDb.get(id)
        }
    }

    /*fun delete(cat: String) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                catDb.delete(cat)
            }
        }
    }*/

    fun makeCategoryEditTextVisible() {
        _categoryEditTextIsOpen.value = true
    }

    fun makeCategoryEditTextNotVisible() {
        _categoryEditTextIsOpen.value = false
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}