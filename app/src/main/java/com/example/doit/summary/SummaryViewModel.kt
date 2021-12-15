package com.example.doit.summary

import androidx.lifecycle.*
import com.example.doit.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalArgumentException

class SummaryViewModel(
    private val summaryDb: SummaryDao,
    private val catDb: CategoryDao, private val todoDb: TodoDbDao
) : ViewModel() {

    val allTodos = todoDb.getAll()

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


    private val _todoCreated = MutableLiveData<Boolean>()
    val todoCreated: LiveData<Boolean>
        get() = _todoCreated

    private val _todoFinished = MutableLiveData<Boolean>()
    val todoFinished: LiveData<Boolean>
        get() = _todoFinished

    private val _deadlineMet = MutableLiveData<Boolean>()
    val deadlineMet: LiveData<Boolean>
        get() = _deadlineMet

    private val _deadlineUnMet = MutableLiveData<Boolean>()
    val deadlineUnMet: LiveData<Boolean>
        get() = _deadlineUnMet

    private val _discarded = MutableLiveData<Boolean>()
    val discarded: LiveData<Boolean>
        get() = _discarded

    private val _mostActive = MutableLiveData<Int>()
    val mostActive: LiveData<Int>
        get() = _mostActive

    private val _leastActive = MutableLiveData<Int>()
    val leastActive: LiveData<Int>
        get() = _leastActive

    private val _mostSuccessful = MutableLiveData<Int>()
    val mostSuccessful: LiveData<Int>
        get() = _mostSuccessful

    private val _leastSuccessful = MutableLiveData<Int>()
    val leastSuccessful: LiveData<Int>
        get() = _leastSuccessful

    fun setIsTodoCreated(value: Boolean) {
        _todoCreated.value = value
    }

    fun setIsTodFinished(value: Boolean) {
        _todoFinished.value = value
    }

    fun setIsTodoDiscarded(value: Boolean) {
        _discarded.value = value
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

    fun updateDiscarded() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                summaryDb.insert(summary.value!!.mapExcept {
                    todosDiscarded += 1
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
    private val summaryDb: SummaryDao,
    private val catDb: CategoryDao, private val todoDb: TodoDbDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SummaryViewModel::class.java))
            return SummaryViewModel(summaryDb, catDb, todoDb) as T
        throw IllegalArgumentException("Unknown view model class")
    }

}