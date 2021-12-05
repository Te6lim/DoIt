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
            text = context.getString(
                R.string.remind_me, context.getString(
                    R.string.date_time_string,

                    with(todo.dateTodo.toLocalDate()) {
                        when {
                            this == LocalDate.now() -> "Today"
                            LocalDate.now() == LocalDate.of(
                                this.year, this.monthValue, this.dayOfMonth - 1
                            ) -> "Tomorrow"
                            else -> this.formatToString(
                                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                            )
                        }
                    }, todo.dateTodo.toLocalTime().formatToString()
                )
            )
        } else visibility = View.GONE
    }
}

@BindingAdapter("setDeadline")
fun TextView.setDeadlineDateString(item: Todo?) {
    item?.let { todo ->
        if (todo.dateFinished != null) {
            visibility = View.VISIBLE
            text =
                context.getString(
                    R.string.finished_date_string,

                    with(todo.dateFinished!!.toLocalDate()) {
                        when {
                            this == LocalDate.now() -> "Today"
                            LocalDate.now() == LocalDate.of(
                                this.year, this.monthValue, this.dayOfMonth + 1
                            ) -> "Yesterday"
                            else -> this.formatToString(
                                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                            )
                        }
                    }, todo.dateFinished?.toLocalTime()?.formatToString()
                )
        } else {
            if (todo.hasDeadline) {
                visibility = View.VISIBLE
                text = context.getString(
                    R.string.deadline_string,

                    with(todo.deadlineDate!!.toLocalDate()) {
                        when {
                            this == LocalDate.now() -> "Today"
                            LocalDate.now() == LocalDate.of(
                                this.year, this.monthValue, this.dayOfMonth - 1
                            ) -> "Tomorrow"
                            else -> this.formatToString(
                                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                            )
                        }
                    }, todo.deadlineDate?.toLocalTime()?.formatToString()
                )
            } else visibility = View.GONE
        }
    }
}