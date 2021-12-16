package com.example.doit.todoList.categories

import androidx.lifecycle.*
import com.example.doit.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class DialogOptions(val value: String) {
    OPTION_A("Edit"), OPTION_B("Set as default"),
    OPTION_C("Clear"), OPTION_D("Delete")
}

class CategoriesViewModel(
    private val catDb: CategoryDao,
    private val todoDb: TodoDbDao,
    private val summaryDb: SummaryDao
) : ViewModel() {

    private val todos = todoDb.getAll()
    val categories = catDb.getAll()

    private val summary = summaryDb.getSummary()
    val readySummary = fetchReadySummary()

    val defaultCategory = Transformations.map(categories) { list ->
        list.find { it.isDefault }
    }

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
            updateMostActive()
            updateLeastActive()
        }
    }

    private fun getList(catList: List<Category>, todoList: List<Todo>): List<CategoryInfo> {
        val catInfoList = mutableListOf<CategoryInfo>()
        catList.forEach { cat ->
            val listFilter = todoList.filter { it.catId == cat.id }
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

    fun changeDefault(id: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                with(catDb) {
                    update(
                        get(id)!!.apply {
                            update(defaultCategory.value!!.apply { isDefault = false })
                            isDefault = true
                        }
                    )
                }
            }
        }
    }

    fun clearCategory(cat: Category) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                todoDb.clearByCategory(cat.id)
            }
        }
    }

    fun deleteCategory(cat: Category) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                todoDb.clearByCategory(cat.id)
                catDb.delete(cat.id)
            }
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                catDb.update(category)
            }
        }
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

    private fun updateMostActive(catId: Int = -1) {
        val cat = categories.value!!.find { it.id == catId }
        val former = categories.value!!.find { it.id == summary.value?.mostActiveCategory }
        if (cat != null) {
            if (cat.totalFinished > former?.totalFinished ?: 0) {
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
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
        var least: Category = categories.value!![0]
        for (i in 1 until categories.value!!.size) {
            if (categories.value!![i].totalFinished < least.totalFinished)
                least = categories.value!![i]
        }
        viewModelScope.launch {
            if (least.id != summary.value!!.mostActiveCategory) {
                withContext(Dispatchers.IO) {
                    summaryDb.insert(summary.value!!.apply {
                        leastActiveCategory = least.id
                    })
                }
            } else {
                withContext(Dispatchers.IO) {
                    summaryDb.insert(summary.value!!.apply {
                        leastActiveCategory = -1
                    })
                }
            }
        }
    }
}

data class CategoryInfo(
    val id: Int,
    var name: String,
    val todoCount: Int,
    val todoCompletedCount: Int,
    val isDefault: Boolean
)

class CategoriesViewModelFactory(
    private val catDb: CategoryDao, private val todoDb: TodoDbDao,
    private val summaryDb: SummaryDao
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoriesViewModel::class.java))
            return CategoriesViewModel(catDb, todoDb, summaryDb) as T

        throw IllegalArgumentException("Unknown view model class")
    }
}