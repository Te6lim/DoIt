package com.te6lim.doit.todoList.createTodo

import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import com.te6lim.doit.R
import com.te6lim.doit.database.Todo
import com.te6lim.doit.todoList.formatToString
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@BindingAdapter("setColor")
fun Button.setColor(item: LiveData<Boolean>?) {
    item?.let {
        if (!it.value!!) {
            if (isEnabled) setBackgroundColor(ContextCompat.getColor(context, R.color.colorRed))
        } else {
            if (isEnabled) setBackgroundColor(ContextCompat.getColor(context, R.color.primaryColor))
        }
    }
}

@BindingAdapter("actionText")
fun Button.actionText(value: Todo?) {
    text = if (value != null) context.getString(R.string.update)
    else context.getString(R.string.create)
}

@BindingAdapter("setDateText")
fun Button.setDateText(date: LiveData<LocalDate?>?) {
    date?.let {
        date.value?.let { d ->
            text = d.formatToString(DateTimeFormatter.ISO_DATE)
        }
    }
}

@BindingAdapter("setTimeText")
fun Button.setTimeText(time: LiveData<LocalTime?>?) {
    time?.let {
        time.value?.let { t ->
            text = t.formatToString()
        }
    }
}

@BindingAdapter("setDeadlineText")
fun Button.setDeadlineText(date: LiveData<LocalDateTime?>?) {
    date?.let {
        if (it.value != null) {
            it.value!!.let { d ->
                text = context.getString(
                    R.string.date_time_string,
                    d.toLocalDate().formatToString(DateTimeFormatter.ISO_DATE),
                    d.toLocalTime().formatToString()
                )
            }
        } else text = context.getString(R.string.set_deadline)
    }
}