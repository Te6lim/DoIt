package com.example.doit.todoList

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.doit.R
import com.example.doit.database.Todo
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@BindingAdapter("setDate")
fun TextView.setDate(item: Todo?) {
    item?.let {
        text = it.dateTodo.toLocalDate().formatToString(
            DateTimeFormatter.ofLocalizedDate(
                FormatStyle.FULL
            )
        )
    }
}

@BindingAdapter("setDeadline")
fun TextView.setDeadlineDateString(item: Todo?) {
    item?.let {
        if (it.hasDeadline) {
            visibility = View.VISIBLE
            text = context.getString(
                R.string.deadline_string, it.deadlineDate?.toLocalDate()?.formatToString(
                    DateTimeFormatter.ISO_DATE
                ), it.deadlineDate?.toLocalTime()?.formatToString()
            )
        } else visibility = View.GONE
    }
}