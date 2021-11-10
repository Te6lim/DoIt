package com.example.doit.todoList.createTodo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.doit.database.Category
import java.time.LocalDate
import java.time.LocalTime

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

    private var deadlineDateUIString = ""

    private val _deadlineUIString = MutableLiveData("SET DEADLINE")
    val deadlineUIString: LiveData<String>
        get() = _deadlineUIString

    private val _isDateValid = MutableLiveData(true)
    val isDateValid: LiveData<Boolean>
        get() = _isDateValid

    private val _isTimeValid = MutableLiveData(true)
    val isTimeValid: LiveData<Boolean>
        get() = _isTimeValid

    private val _isDeadlineValid = MutableLiveData(true)
    val isDeadLineValid: LiveData<Boolean>
        get() = _isDeadlineValid

    private val _isTodoValid = MutableLiveData<Boolean>()
    val isTodoValid: LiveData<Boolean>
        get() = _isTodoValid

    fun todoValid(): Boolean {
        return (description.isNotEmpty() &&
                (!deadlineEnabled || (deadlineEnabled && deadlineDate != null &&
                        deadlineTime != null)) &&
                _isDateValid.value!! && _isTimeValid.value!!
                )
    }

    fun setDescription(d: String) {
        description = d
        _isTodoValid.value = todoValid()
    }

    fun setIsDeadlineEnabled(value: Boolean) {
        deadlineEnabled = value
        _isTodoValid.value = todoValid()
    }

    fun setDate(year: Int, month: Int, day: Int) {
        if (dateIsInvalid(year, month, day, LocalDate.now()))
            invalidateDate()
        else {
            _dateUIString.value = "$year-$month-$day"
            dateSet = LocalDate.of(year, month, day)
            _isDateValid.value = true
            _isTodoValid.value = todoValid()

            if (deadlineDate != null) {
                if (dateIsInvalid(
                        deadlineDate!!.year, deadlineDate!!.monthValue,
                        deadlineDate!!.dayOfMonth, dateSet
                    )
                ) invalidateDate()
                else if (timeIsInvalid(
                        deadlineTime!!.hour, deadlineTime!!.minute,
                        dateSet, deadlineDate!!, timeSet
                    )
                ) invalidateTime()
            }
        }
    }

    fun setTime(hour: Int, minute: Int) {
        if (timeIsInvalid(hour, minute, dateSet, LocalDate.now(), LocalTime.now()))
            invalidateTime()
        else {
            timeSet = LocalTime.of(hour, minute)
            val meridian = if (hour < 12) "AM" else "PM"
            _timeUIString.value = "${hour % 12}:$minute $meridian"
            _isTimeValid.value = true
            _isTodoValid.value = todoValid()

            if (deadlineTime != null) {
                if (timeIsInvalid(
                        deadlineTime!!.hour, deadlineTime!!.minute,
                        deadlineDate!!, dateSet, timeSet
                    )
                ) invalidateTime()
            }
        }
    }

    private fun invalidateDate() {
        _isDateValid.value = false
        _dateUIString.value = "INVALID DATE!"
        dateSet = LocalDate.now()
        _isTodoValid.value = todoValid()
    }

    private fun invalidateTime() {
        _isTimeValid.value = false
        _timeUIString.value = "INVALID TIME!"
        timeSet = LocalTime.now()
        _isTodoValid.value = todoValid()
    }

    fun setDeadlineDate(year: Int, month: Int, day: Int) {
        if (!_isDateValid.value!! || !isTimeValid.value!! ||
            dateIsInvalid(year, month, day, dateSet)
        ) invalidateDeadlineDate()
        else {
            deadlineDateUIString = "$year-$month-$day"
            deadlineDate = LocalDate.of(year, month, day)
            _isDeadlineValid.value = true

            val hour = 23
            val minute = 59
            deadlineTime = LocalTime.of(hour, minute)
            val meridian = if (hour < 12) "AM" else "PM"
            _deadlineUIString.value = deadlineDateUIString.plus(
                " by ${hour % 12}:$minute $meridian"
            )
            _isTodoValid.value = todoValid()
        }
    }

    private fun invalidateDeadlineDate() {
        _deadlineUIString.value = "INVALID DATE!"
        _isDeadlineValid.value = false
        deadlineDate = null
        _isTodoValid.value = todoValid()
    }

    fun setDeadlineTime(hour: Int, minute: Int) {
        if (_isDeadlineValid.value!!) {
            if (!_isDateValid.value!! || !isTimeValid.value!! ||
                timeIsInvalid(hour, minute, deadlineDate, dateSet, timeSet)
            ) invalidateDeadlineTime()
            else {
                deadlineTime = LocalTime.of(hour, minute)
                val meridian = if (hour < 12) "AM" else "PM"
                _deadlineUIString.value = deadlineDateUIString.plus(
                    " by ${hour % 12}:$minute $meridian"
                )
                _isDeadlineValid.value = true
                _isTodoValid.value = todoValid()
            }
        }
    }

    private fun invalidateDeadlineTime() {
        _isDeadlineValid.value = false
        _deadlineUIString.value = "INVALID TIME!"
        deadlineTime = null
        _isTodoValid.value = todoValid()
    }

    private fun dateIsInvalid(
        year: Int, month: Int, day: Int,
        referenceDate: LocalDate
    ): Boolean {
        val y = referenceDate.year
        val m = referenceDate.monthValue
        val d = referenceDate.dayOfMonth
        if (year < y) return true
        if (year == y && month < m) return true
        if (year == y && month == m && day < d) return true
        return false
    }

    private fun timeIsInvalid(
        hour: Int, minute: Int, date: LocalDate?,
        referenceDate: LocalDate, referenceTime: LocalTime
    ): Boolean {
        val presentHour = referenceTime.hour
        val presentMinute = referenceTime.minute
        return if (date == referenceDate) {
            ((hour < presentHour) || (hour == presentHour && minute < presentMinute))
        } else false
    }

    fun setCategory(cat: Category) {
        category = cat.name
    }
}