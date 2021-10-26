package com.example.doit.todoList

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.doit.database.Todo

@BindingAdapter("setDate")
fun TextView.setDate(item: Todo?) {
    item?.let {
        text = it.dateTodo.formatToString()
    }
}