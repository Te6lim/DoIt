package com.example.doit.todoList

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.doit.R
import com.example.doit.database.Todo
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@BindingAdapter("setDate")
fun TextView.setDate(item: Todo?) {
    item?.let { todo ->
        if (todo.dateFinished == null) {
            text = if (todo.dateTodo.toLocalDate() == LocalDate.now()) {
                context.getString(
                    R.string.remind_me, context.getString(
                        R.string.date_time_string, "Today",
                        todo.dateTodo.toLocalTime().formatToString()
                    )
                )
            } else {
                context.getString(
                    R.string.remind_me, context.getString(
                        R.string.date_time_string,
                        todo.dateTodo.toLocalDate().formatToString(
                            DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                        ), todo.dateTodo.toLocalTime().formatToString()
                    )
                )
            }
        } else visibility = View.GONE
    }
}

@BindingAdapter("setDeadline")
fun TextView.setDeadlineDateString(item: Todo?) {
    item?.let { todo ->
        if (todo.dateFinished != null) {
            visibility = View.VISIBLE
            text = if (todo.dateFinished!!.toLocalDate() == LocalDate.now()) {
                context.getString(
                    R.string.finished_date_string,
                    "Today", todo.dateFinished?.toLocalTime()?.formatToString()
                )
            } else {
                context.getString(
                    R.string.finished_date_string, todo.dateFinished?.toLocalDate()?.formatToString(
                        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                    ), todo.dateFinished?.toLocalTime()?.formatToString()
                )
            }
        } else {
            if (todo.hasDeadline) {
                visibility = View.VISIBLE
                text = if (todo.deadlineDate!!.toLocalDate() == LocalDate.now()) {
                    context.getString(
                        R.string.deadline_string,
                        "Today", todo.deadlineDate?.toLocalTime()?.formatToString()
                    )
                } else {
                    context.getString(
                        R.string.deadline_string, todo.deadlineDate?.toLocalDate()?.formatToString(
                            DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                        ), todo.deadlineDate?.toLocalTime()?.formatToString()
                    )
                }
            } else visibility = View.GONE
        }
    }
}