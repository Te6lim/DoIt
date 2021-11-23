package com.example.doit.todoList.categories

import androidx.lifecycle.*
import com.example.doit.database.CategoryDao
import com.example.doit.database.TodoDbDao

class CategoriesViewModel(catDb: CategoryDao, todoDb: TodoDbDao) : ViewModel() {

    private val todos = todoDb.getAll()
    private val categories = catDb.getAll()

    private val _catListInfo = MutableLiveData<List<CategoryInfo>>()
    val catListInfo: LiveData<List<CategoryInfo>>
        get() = _catListInfo


    val todoListTranform = Transformations.map(todos) { todoList ->
        val catInfoList = mutableListOf<CategoryInfo>()
        categories.value?.let { catList ->
            catList.forEach { cat ->
                val listFilter = todoList!!.filter { it.category == cat.name }
                catInfoList.add(
                    CategoryInfo(
                        cat.name,
                        listFilter.filter { !it.isCompleted }.size,
                        listFilter.filter { it.isCompleted }.size
                    )
                )
            }
        }

        _catListInfo.value = catInfoList
    }

    val categoriesTransform = Transformations.map(categories) { catList ->
        val catInfoList = mutableListOf<CategoryInfo>()
        todos.value?.let { todoList ->
            catList.forEach { cat ->
                val listFilter = todoList.filter { it.category == cat.name }
                catInfoList.add(
                    CategoryInfo(
                        cat.name,
                        listFilter.filter { !it.isCompleted }.size,
                        listFilter.filter { it.isCompleted }.size
                    )
                )
            }
        }
        _catListInfo.value = catInfoList
    }
}

data class CategoryInfo(
    val name: String,
    val todoCount: Int,
    val todoCompletedCount: Int,
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