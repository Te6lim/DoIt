package com.example.doit.todoList.createTodo

import android.os.Parcel
import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.doit.database.Category
import com.example.doit.todoList.formatToString
import java.time.LocalDate
import java.time.LocalTime


class TodoInfo() : Parcelable {
    var description: String = ""

    var dateSet: String = ""
    private set

    var timeSet: String = ""
    private set

    var deadlineDate: String = ""
    private set

    var deadlineTime: String = ""
    private set

    var deadlineEnabled: Boolean = false
    private set
    var category: String  = CreateTodoViewModel.DEFAULT_CATEGORY
    private set

    private val _valid = MutableLiveData<Boolean>()
    val valid: LiveData<Boolean>
    get() =_valid


    fun isValid(): Boolean {
        return dateSet.isNotEmpty() || timeSet.isNotEmpty() || description.isNotEmpty()
    }

    fun setDate(y: Int, m: Int, d: Int) {
        dateSet = LocalDate.of(y, m, d).formatToString()
        if (timeSet.isNotEmpty()) _valid.value = true
    }

    fun setTime(h: Int, m: Int) {
        timeSet = LocalTime.of(h, m).formatToString()
        if (dateSet.isNotEmpty()) _valid.value = true
    }

    fun setCategory(cat: Category) {
        category = cat.name
    }

    private constructor(parcel: Parcel) : this() {
        description = parcel.readString()!!
        dateSet = parcel.readString()!!
        timeSet = parcel.readString()!!
        deadlineDate = parcel.readString()!!
        deadlineTime = parcel.readString()!!
        deadlineEnabled = parcel.readByte() != 0.toByte()
        category = parcel.readString()!!
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(description)
        parcel.writeString(dateSet)
        parcel.writeString(timeSet)
        parcel.writeString(deadlineDate)
        parcel.writeString(deadlineTime)
        parcel.writeByte(if (deadlineEnabled) 1 else 0)
        parcel.writeString(category)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TodoInfo> {
        override fun createFromParcel(parcel: Parcel): TodoInfo {
            return TodoInfo(parcel)
        }

        override fun newArray(size: Int): Array<TodoInfo?> {
            return arrayOfNulls(size)
        }
    }
}