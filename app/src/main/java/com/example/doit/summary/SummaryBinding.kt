package com.example.doit.summary

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.doit.database.Summary

@BindingAdapter("count")
fun TextView.todoCount(count: Int?) {
    count?.let {
        text = count.toString()
    }
}