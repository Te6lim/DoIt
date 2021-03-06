package com.te6lim.doit.todoList

import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.te6lim.doit.R
import com.te6lim.doit.database.Todo
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@BindingAdapter("setDate")
fun TextView.setDate(item: Todo?) {
    item?.let { todo ->
        text = context.getString(
            R.string.date_time_string,
            with(todo.dateTodo.toLocalDate()) {
                val day = this.dayOfMonth - 1
                when {
                    this == LocalDate.now() -> "Today"

                    day in (1..LocalDate.now().month.length(
                        (this.year % 100 == 0 && this.year % 4 == 0 || this.year % 400 == 0)
                    )) && LocalDate.now() == LocalDate.of(
                        this.year, this.monthValue, day
                    ) -> {
                        "Tomorrow"
                    }

                    else -> this.formatToString(
                        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                    )
                }
            }, todo.dateTodo.toLocalTime().formatToString()
        )
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
                        val day = this.dayOfMonth + 1
                        when {
                            this == LocalDate.now() -> "Today"

                            day in (1..LocalDate.now().month.length(
                                (this.year % 100 == 0 && this.year % 4 == 0 || this.year % 400 == 0)
                            )) && LocalDate.now() == LocalDate.of(
                                this.year, this.monthValue, day
                            ) -> {
                                "Yesterday"
                            }

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
                        val day = this.dayOfMonth - 1
                        when {
                            this == LocalDate.now() -> "Today"

                            day in (1..LocalDate.now().month.length(
                                (this.year % 100 == 0 && this.year % 4 == 0 || this.year % 400 == 0)
                            )) && LocalDate.now() == LocalDate.of(
                                this.year, this.monthValue, day
                            ) -> {
                                "Tomorrow"
                            }

                            else -> this.formatToString(
                                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                            )
                        }
                    }, todo.deadlineDate?.toLocalTime()?.formatToString()
                )
            } else visibility = View.GONE
        }

        if (item.isLate)
            setTextColor(ContextCompat.getColor(context, R.color.deadline_passed))
        else setTextColor(ContextCompat.getColor(context, R.color.secondaryColor))
    }
}