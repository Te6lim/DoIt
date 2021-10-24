package com.example.doit.todoList.createTodo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.doit.database.Category
import com.example.doit.database.CategoryDao
import kotlinx.coroutines.*

class CreateTodoViewModel(private val catDb: CategoryDao) : ViewModel() {

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

    private var _defaultCategory = MutableLiveData<Category>()
    val defaultCategory: LiveData<Category>
    get() = _defaultCategory

    val categories = catDb.getAll()

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

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}