package com.example.doit.todoList

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.doit.R
import com.example.doit.database.Todo
import com.example.doit.todoList.createTodo.CreateTodoViewModel
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
    item?.let { todo ->
        if (todo.dateFinished != null) {
            visibility = View.VISIBLE
            text = context.getString(
                R.string.finished_date_string, todo.dateFinished?.toLocalDate()?.formatToString(
                    DateTimeFormatter.ISO_DATE
                ), todo.dateFinished?.toLocalTime()?.formatToString()
            )
        } else {
            if (todo.hasDeadline) {
                visibility = View.VISIBLE
                text = context.getString(
                    R.string.deadline_string, todo.deadlineDate?.toLocalDate()?.formatToString(
                        DateTimeFormatter.ISO_DATE
                    ), todo.deadlineDate?.toLocalTime()?.formatToString()
                )
            } else visibility = View.GONE
        }
    }
}