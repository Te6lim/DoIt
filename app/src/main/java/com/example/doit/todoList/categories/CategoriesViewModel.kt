package com.example.doit.todoList.categories

import androidx.lifecycle.*
import com.example.doit.database.Category
import com.example.doit.database.CategoryDao
import com.example.doit.database.Todo
import com.example.doit.database.TodoDbDao

enum class DialogOptions(val value: String) {
    OPTION_A("Make default"), OPTION_B("Clear"), OPTION_C("Delete")
}

class CategoriesViewModel(catDb: CategoryDao, todoDb: TodoDbDao) : ViewModel() {

    private val todos = todoDb.getAll()
    private val categories = catDb.getAll()

    private val _catListInfo = MutableLiveData<List<CategoryInfo>>()
    val catListInfo: LiveData<List<CategoryInfo>>
        get() = _catListInfo


    val todoListTransform = Transformations.map(todos) { todoList ->
        categories.value?.let { catList ->
            todoList?.let {
                _catListInfo.value = getList(catList, it)
            }
        }
    }

    val categoriesTransform = Transformations.map(categories) { catList ->
        todos.value?.let { todoList ->
            _catListInfo.value = getList(catList, todoList)
        }
    }

    fun categoriesList(): List<Category> = categories.value!!

    private val _longClicked = MutableLiveData<Boolean>()
    val longClicked: LiveData<Boolean>
        get() = _longClicked

    private fun getList(catList: List<Category>, todoList: List<Todo>): List<CategoryInfo> {
        val catInfoList = mutableListOf<CategoryInfo>()
        catList.forEach { cat ->
            val listFilter = todoList.filter { it.category == cat.name }
            catInfoList.add(
                CategoryInfo(
                    cat.id,
                    cat.name,
                    listFilter.filter { !it.isFinished }.size,
                    listFilter.filter { it.isFinished }.size,
                    cat.isDefault
                )
            )
        }
        return catInfoList
    }
}

data class CategoryInfo(
    val id: Int,
    val name: String,
    val todoCount: Int,
    val todoCompletedCount: Int,
    val isDefault: Boolean
)

class CategoriesViewModelFactory(
    private val catDb: CategoryDao, private val todoDb: TodoDbDao
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoriesViewModel::class.java))
            return CategoriesViewModel(catDb, todoDb) as T

        throw IllegalArgumentException("Unknown view model class")
    }
}