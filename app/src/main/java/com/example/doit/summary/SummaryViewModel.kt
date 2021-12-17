package com.example.doit.summary

import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.example.doit.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalArgumentException

class SummaryViewModel(
    private val summaryDb: SummaryDao,
    private val catDb: CategoryDao
) : ViewModel() {

    private val summary = summaryDb.getSummary()

    val readySummary = fetchReadySummary()

    private val _mostActive = MutableLiveData<Category>()
    val mostActive: LiveData<Category>
        get() = _mostActive

    private val _leastActive = MutableLiveData<Category>()
    val leastActive: LiveData<Category>
        get() = _leastActive

    private val _mostSuccessFul = MutableLiveData<String>()
    val mostSuccessful: LiveData<String>
        get() = _mostSuccessFul


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
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        _mostActive.postValue(
                            catDb.get(it.mostActiveCategory)
                        )
                        _leastActive.postValue(
                            catDb.get(it.leastActiveCategory)
                        )
                        val cat = catDb.get(it.mostSuccessfulCategory)
                        _mostSuccessFul.postValue(
                            "${cat?.name}: ${it.mostSuccessfulRatio}%"
                        )
                    }
                }
            }
        }
        result.addSource(summary, action)
        return result
    }
}

class SummaryViewModelFactory(
    private val summaryDb: SummaryDao,
    private val catDb: CategoryDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SummaryViewModel::class.java))
            return SummaryViewModel(summaryDb, catDb) as T
        throw IllegalArgumentException("Unknown view model class")
    }

}