package com.example.doit.summary

import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.example.doit.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalArgumentException

class SummaryViewModel(
    private val summaryDb: SummaryDao
) : ViewModel() {

    private val summary = summaryDb.getSummary()

    val readySummary = fetchReadySummary()

    private fun fetchReadySummary(): LiveData<Summary> {
        val result = MediatorLiveData<Summary>()
        val action = Observer<Summary?> {
            if (it == null) {
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        summaryDb.insert(Summary())
                    }
                }
            } else
                result.value = it
        }
        result.addSource(summary, action)
        return result
    }

    fun updateFinishedCount(value: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (value)
                    summaryDb.insert(summary.value!!.mapExcept {
                        todosFinished += 1
                    })
                else summaryDb.insert(summary.value!!.mapExcept {
                    todosFinished -= 1
                })
            }
        }
    }

    fun updateCreatedCount() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                summaryDb.insert(summary.value!!.mapExcept {
                    todosCreated += 1
                })
            }
        }
    }

    fun updateDeadlineStatus(todo: Todo) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (todo.hasDeadline) {
                    if (todo.dateFinished!!.toLocalDate() <= todo.deadlineDate!!.toLocalDate()) {
                        summaryDb.insert(summary.value!!.mapExcept {
                            deadlinesMet += 1
                        })
                    } else {
                        summaryDb.insert(summary.value!!.mapExcept {
                            deadlinesUnmet += 1
                        })
                    }
                }
            }
        }
    }

    fun updateDiscarded() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                summaryDb.insert(summary.value!!.mapExcept {
                    todosDiscarded += 1
                })
            }
        }
    }

    fun updateMostActive(allCategories: List<Category>, catId: Int = -1) {
        val mostActiveCount = summary.value!!.mostActiveCategoryCount
        val category = allCategories.find { it.id == catId }

        if (category != null) {
            if (category.totalFinished > mostActiveCount) {
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        summaryDb.insert(summary.value!!.apply {
                            mostActiveCategory = category.name
                            mostActiveCategoryCount = category.totalFinished
                        })
                    }
                }
            }
        } else {
            findNextMostActive(allCategories)
        }
    }

    private fun findNextMostActive(allCategories: List<Category>) {
        var countFinished = 0
        var countCreated = 0
        var category = summary.value!!.mostActiveCategory
        allCategories.forEach { cat ->
            if (cat.totalFinished > countFinished) {
                countFinished = cat.totalFinished
                countCreated = cat.totalCreated
                category = cat.name
            } else if (cat.totalFinished == countFinished) {
                if (cat.totalCreated >= countCreated) {
                    countFinished = cat.totalFinished
                }
            }
        }

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                summaryDb.insert(summary.value!!.apply {
                    mostActiveCategory = category
                    mostActiveCategoryCount = countFinished
                })
            }
        }
    }

    fun updateLeastActive(allCategories: List<Category>, catId: Int = -1) {
        val leastActiveCount = summary.value!!.leastActiveCategoryCount
        val category = allCategories.find { it.id == catId }

        if (category != null) {
            if (category.totalFinished < leastActiveCount) {
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        summaryDb.insert(summary.value!!.apply {
                            leastActiveCategory = category.name
                            leastActiveCategoryCount = category.totalFinished
                        })
                    }
                }
            }
        } else {
            findNextLeastActive(allCategories)
        }
    }

    private fun findNextLeastActive(allCategories: List<Category>) {
        var countFinished = 0
        var countCreated = 0
        var category = summary.value!!.leastActiveCategory
        allCategories.forEach { cat ->
            if (cat.totalFinished < countFinished) {
                countFinished = cat.totalFinished
                countCreated = cat.totalCreated
                category = cat.name
            } else if (cat.totalFinished == countFinished) {
                if (cat.totalCreated < countCreated) {
                    countFinished = cat.totalFinished
                }
            }
        }

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                summaryDb.insert(summary.value!!.apply {
                    leastActiveCategory = category
                    leastActiveCategoryCount = countFinished
                })
            }
        }
    }

    private fun Summary.mapExcept(x: Summary.() -> Unit): Summary {
        this.x()
        return this
    }
}

class SummaryViewModelFactory(
    private val summaryDb: SummaryDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SummaryViewModel::class.java))
            return SummaryViewModel(summaryDb) as T
        throw IllegalArgumentException("Unknown view model class")
    }

}