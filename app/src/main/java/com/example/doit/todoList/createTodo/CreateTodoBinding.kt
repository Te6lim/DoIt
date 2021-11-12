package com.example.doit.todoList.createTodo

import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import com.example.doit.R

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