package com.example.doit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MainViewModel(startDest: Int) : ViewModel() {

    private val _activeStartDestination = MutableLiveData<Int>()
    val activeStartDestination: LiveData<Int>
        get() = _activeStartDestination

    private val _contextActionbarActive = MutableLiveData<Boolean>()
    val contextActionbarActive: LiveData<Boolean>
        get() = _contextActionbarActive

    init {
        _activeStartDestination.value = startDest
    }

    fun setActiveStartDestination(id: Int) {
        _activeStartDestination.value = id
    }

    fun setContextActionbarActive(value: Boolean) {
        _contextActionbarActive.value = value
    }
}

class MainViewModelFactory(private val startDest: Int) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java))
            return MainViewModel(startDest) as T
        throw IllegalArgumentException("Unknown viewModel class")
    }
}