package com.example.doit.summary

import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("count")
fun TextView.todoCount(count: Int?) {
    count?.let {
        text = count.toString()
    }
}

@BindingAdapter("categoryName")
fun TextView.mostActive(name: String?) {
    name?.let {
        text = name
    } ?: run { text = "-" }
}