package com.te6lim.doit.todoList.createTodo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class TodoModel {
    private val _description = MutableLiveData<String>()
    val description: LiveData<String>
        get() = _description

    private val _dateTodoLive = MutableLiveData<LocalDate?>()
    val dateTodoLive: LiveData<LocalDate?>
        get() = _dateTodoLive

    private val _timeTodoLive = MutableLiveData<LocalTime?>()
    val timeTodoLive: LiveData<LocalTime?>
        get() = _timeTodoLive

    private val _deadlineDateLive = MutableLiveData<LocalDateTime?>()
    val deadlineDateLive: LiveData<LocalDateTime?>
        get() = _deadlineDateLive

    private val _dateTodoIsValid = MutableLiveData(true)
    val dateTodoIsValid: LiveData<Boolean>
        get() = _dateTodoIsValid

    private val _timeTodoIsValid = MutableLiveData(true)
    val timeTodoIsValid: LiveData<Boolean>
        get() = _timeTodoIsValid

    private val _deadlineDateIsValid = MutableLiveData(false)
    val deadlineDateIsValid: LiveData<Boolean>
        get() = _deadlineDateIsValid

    private val _isTodoValid = MutableLiveData(false)
    val isTodoValid: LiveData<Boolean>
        get() = _isTodoValid

    private val _hasDeadline = MutableLiveData(false)
    val hasDeadline: LiveData<Boolean>
        get() = _hasDeadline

    fun setDescription(value: String) {
        _description.value = value
        isValid()
    }

    fun setDateTodo(year: Int, month: Int, day: Int) {
        if (_deadlineDateLive.value != null) {
            _deadlineDateLive.value = null
            _deadlineDateIsValid.value = false
        }

        val date = LocalDate.of(year, month, day)
        _timeTodoIsValid.value = timeIsValid(timeTodoLive.value ?: LocalTime.now(), date)
        _dateTodoIsValid.value = dateIsValid(date, LocalDate.now())

        _dateTodoLive.value = date

        isValid()
    }

    fun setTimeTodo(hour: Int, minute: Int) {
        if (_deadlineDateLive.value != null) {
            _deadlineDateLive.value = null
            _deadlineDateIsValid.value = false
        }

        val time = LocalTime.of(hour, minute)
        _timeTodoIsValid.value = timeIsValid(time, dateTodoLive.value ?: LocalDate.now())

        _timeTodoLive.value = time

        isValid()
    }

    fun setDeadlineDate(year: Int, month: Int, day: Int) {
        val deadlineDate = LocalDate.of(year, month, day)

        _deadlineDateIsValid.value = dateIsValid(
            deadlineDate,
            dateTodoLive.value ?: LocalDate.now()
        )

        _deadlineDateLive.value = LocalDateTime.of(
            deadlineDate, LocalTime.of(23, 59)
        )

        isValid()
    }

    fun setDeadlineTime(hour: Int, minute: Int) {
        val deadlineTime = LocalTime.of(hour, minute)

        _deadlineDateIsValid.value =
            timeIsValid(deadlineTime, deadlineDateLive.value!!.toLocalDate())

        _deadlineDateLive.value = LocalDateTime.of(
            deadlineDateLive.value!!.toLocalDate(), deadlineTime
        )

        isValid()
    }

    private fun dateIsValid(date: LocalDate, referenceDate: LocalDate): Boolean {
        if (date.year < referenceDate.year) return false
        if (date.year == referenceDate.year && date.monthValue < referenceDate.monthValue)
            return false
        if (date.year == referenceDate.year && date.monthValue == referenceDate.monthValue &&
            date.dayOfMonth < referenceDate.dayOfMonth
        ) return false
        return true
    }

    private fun timeIsValid(time: LocalTime, date: LocalDate): Boolean {
        if (date != LocalDate.now()) return true
        if (time.hour < LocalTime.now().hour) return false
        if (time.hour == LocalTime.now().hour && time.minute < LocalTime.now().minute) return false
        return true
    }

    private fun isValid() {
        if (hasDeadline.value!!) {

            if (_description.value.isNullOrEmpty() || !dateTodoIsValid.value!! ||
                !timeTodoIsValid.value!! || !deadlineDateIsValid.value!!
            ) {
                if (_isTodoValid.value!!) _isTodoValid.value = false
            } else if (!_isTodoValid.value!!) _isTodoValid.value = true

        } else {

            if (_description.value.isNullOrEmpty() || !dateTodoIsValid.value!! ||
                !timeTodoIsValid.value!!
            ) {
                if (_isTodoValid.value!!) _isTodoValid.value = false
            } else if (!_isTodoValid.value!!) _isTodoValid.value = true

        }
    }

    fun setHasDeadlineEnabled(value: Boolean) {
        _hasDeadline.value = value
        isValid()
    }
}