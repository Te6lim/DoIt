package com.example.doit.summary

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.doit.database.Summary

@BindingAdapter("finishedTodoCount")
fun TextView.finishedTodoCont(count: Int?) {
    count?.let {
        text = count.toString()
    }
}