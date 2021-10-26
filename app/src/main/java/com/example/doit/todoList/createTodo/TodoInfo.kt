package com.example.doit.todoList.createTodo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.doit.database.Category
import java.time.LocalDate
import java.time.LocalTime
import java.util.*


class TodoInfo {
    var description: String = ""
        private set

    var dateSet: LocalDate = LocalDate.now()
        private set

    var timeSet: LocalTime = LocalTime.now()
        private set

    var deadlineDate: LocalDate? = null
        private set

    var deadlineTime: LocalTime? = null
        private set

    var deadlineEnabled: Boolean = false
        private set
    var category: String = CreateTodoViewModel.DEFAULT_CATEGORY
        private set

    private val _dateUIString = MutableLiveData("DATE")
    val dateUIString: LiveData<String>
        get() = _dateUIString

    private val _timeUIString = MutableLiveData("TIME")
    val timeUIString: LiveData<String>
        get() = _timeUIString

    private val _isDateValid = MutableLiveData(true)
    val isDateValid: LiveData<Boolean>
        get() = _isDateValid

    private val _isTimeValid = MutableLiveData(true)
    val isTimeValid: LiveData<Boolean>
        get() = _isTimeValid

    private val _isTodoValid = MutableLiveData<Boolean>()
    val isTodoValid: LiveData<Boolean>
        get() = _isTodoValid

    fun todoValid(): Boolean {
        return description.isNotEmpty()
    }

    fun setDescription(d: String) {
        description = d
        _isTodoValid.value = todoValid()
    }

    fun setDate(year: Int, month: Int, day: Int) {
        if (dateIsInvalid(year, month, day)) {
            _isDateValid.value = false
            _dateUIString.value = "INVALID DATE!"
        } else {
            _dateUIString.value = "$year-$month-$day"
            dateSet = LocalDate.of(year, month, day)
            _isTodoValid.value = todoValid()
            _isDateValid.value = true
        }
    }

    fun setTime(hour: Int, minute: Int) {
        if (timeIsInvalid(hour, minute)) {
            _isTimeValid.value = false
            _timeUIString.value = "INVALID TIME!"
        } else {
            timeSet = LocalTime.of(hour, minute)
            val meridian = if (hour < 12) "AM" else "PM"
            _timeUIString.value = "${hour % 12}:$minute $meridian"
            _isTodoValid.value = todoValid()
            _isTimeValid.value = true
        }
    }

    private fun dateIsInvalid(year: Int, month: Int, day: Int): Boolean {
        val calendar = Calendar.getInstance()
        val presentYear = calendar.get(Calendar.YEAR)
        val presentMonth = calendar.get(Calendar.MONTH) + 1
        val presentDay = calendar.get(Calendar.DAY_OF_MONTH)
        if (year < presentYear)
            return true
        if (year == presentYear && month < presentMonth)
            return true
        if (year == presentYear && month == presentMonth && day < presentDay)
            return true
        return false
    }

    private fun timeIsInvalid(hour: Int, minute: Int): Boolean {
        val presentHour = LocalTime.now().hour
        val presentMinute = LocalTime.now().minute
        return if (dateSet == LocalDate.now()) {
            ((hour < presentHour) || (hour == presentHour && minute < presentMinute))
        } else false
    }

    fun setCategory(cat: Category) {
        category = cat.name
    }
}