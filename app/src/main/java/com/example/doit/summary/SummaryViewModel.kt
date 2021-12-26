package com.example.doit.summary

import androidx.lifecycle.*
import com.example.doit.database.Category
import com.example.doit.database.CategoryDao
import com.example.doit.database.Summary
import com.example.doit.database.SummaryDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SummaryViewModel(
    private val summaryDb: SummaryDao,
    private val catDb: CategoryDao
) : ViewModel() {

    private val summary = summaryDb.getSummaryLive()
    val categories = catDb.getAllLive()

    val readySummary = fetchReadySummary()

    private val _mostActive = MutableLiveData<Category?>()
    val mostActive: LiveData<Category?>
        get() = _mostActive

    private val _leastActive = MutableLiveData<Category?>()
    val leastActive: LiveData<Category?>
        get() = _leastActive

    private val _mostSuccessFul = MutableLiveData<String?>()
    val mostSuccessful: LiveData<String?>
        get() = _mostSuccessFul

    private val _leastSuccessful = MutableLiveData<String?>()
    val leastSuccessful: LiveData<String?>
        get() = _leastSuccessful


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
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        catDb.get(it.mostActiveCategory)?.let { category ->
                            _mostActive.postValue(category)
                        } ?: _mostActive.postValue(null)

                        catDb.get(it.leastActiveCategory)?.let { category ->
                            _leastActive.postValue(category)
                        } ?: _leastActive.postValue(null)

                        catDb.get(it.mostSuccessfulCategory)?.let { category ->
                            _mostSuccessFul.postValue(
                                "${category.name}: ${it.mostSuccessfulRatio}%"
                            )
                        } ?: _mostSuccessFul.postValue(null)

                        catDb.get(it.leastSuccessfulCategory)?.let { category ->
                            _leastSuccessful.postValue(
                                "${category.name}: ${it.leastSuccessfulRatio}%"
                            )
                        } ?: _leastSuccessful.postValue(null)

                        result.postValue(it)
                    }
                }
            }
        }
        result.addSource(summary, action)
        return result
    }

    fun resetSummary() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val s = Summary()
                summaryDb.insert(summary.value!!.apply {
                    todosCreated = s.todosCreated
                    todosFinished = s.todosFinished
                    deadlinesMet = s.deadlinesMet
                    deadlinesUnmet = s.deadlinesUnmet
                    todosDiscarded = s.todosDiscarded
                    mostActiveCategory = s.mostActiveCategory
                    leastActiveCategory = s.leastActiveCategory
                    mostSuccessfulCategory = s.mostSuccessfulCategory
                    mostSuccessfulRatio = s.mostSuccessfulRatio
                    leastSuccessfulCategory = s.leastSuccessfulCategory
                    leastSuccessfulRatio = s.leastSuccessfulRatio
                })

                categories.value!!.forEach {
                    catDb.update(it.apply {
                        totalCreated = 0
                        totalFinished = 0
                        totalSuccess = 0
                        totalFailure = 0
                    })
                }
            }
        }
    }
}

class SummaryViewModelFactory(
    private val summaryDb: SummaryDao,
    private val catDb: CategoryDao
) : ViewModelProvider.Factory {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SummaryViewModel::class.java))
            return SummaryViewModel(summaryDb, catDb) as T
        throw IllegalArgumentException("Unknown view model class")
    }

}