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
    private val todoDb: TodoDbDao, private val catDb: CategoryDao
) : ViewModel() {

    companion object {
        const val DEFAULT_CATEGORY = "Work"
    }

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _todoInfo = MutableLiveData<TodoInfo>()
    val todoInfo: LiveData<TodoInfo>
        get() = _todoInfo

    var todo = TodoInfo()
        private set

    private val _defaultCategory = MutableLiveData<Category>()
    val defaultCategory: LiveData<Category>
        get() = _defaultCategory

    private val _categoryEditTextIsOpen = MutableLiveData<Boolean>()
    val categoryEditTextIsOpen: LiveData<Boolean>
        get() = _categoryEditTextIsOpen

    val categories = catDb.getAll()

    fun add(todoInfo: TodoInfo) {
        uiScope.launch {

            val todo = Todo(
                todoString = todoInfo.description,
                category = todoInfo.category,
                dateTodo = todoInfo.dateSet,
                timeTodo = todoInfo.timeSet
            )
            addTodo(todo)
        }
    }

    suspend fun addTodo(todo: Todo) {
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

    fun initializeDefault() {
        uiScope.launch {
            _defaultCategory.value = defaultCategory()
        }
    }

    private suspend fun defaultCategory(): Category {
        return withContext(Dispatchers.IO) {
            val default = catDb.getDefault(true)
            default
        }
    }

    fun addNewCategory(newCategory: String, default: Boolean = false) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                catDb.insert(Category(name = newCategory,isDefault = default))
            }
        }
    }

    fun getCategoryById(id: Int): Category? {
        var cat: Category? = null
        uiScope.launch {
            cat = getCat(id)
        }
        return cat
    }

    private suspend fun getCat(id: Int): Category? {
        return withContext(Dispatchers.IO) {
            catDb.get(id.toLong())
        }
    }

    fun delete(cat: String) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                catDb.delete(cat)
            }
        }
    }

    fun changeDefault(categoryId: Long) {
        _defaultCategory.value?.let {
            it.isDefault = false
            var newDefault: Category?
            uiScope.launch {
                withContext(Dispatchers.IO) {
                    newDefault = catDb.get(categoryId)
                }
                newDefault?.isDefault = true
                _defaultCategory.value = newDefault ?: it.apply { isDefault = true }
            }
        }
    }

    fun isDefault(catId: Int): Boolean {
        _defaultCategory.value?.let {
            return it.id == catId
        }
        return false
    }

    fun initializeCategories() {
        addNewCategory(DEFAULT_CATEGORY, true)
        addNewCategory("School")
    }

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