package com.example.doit.todoList.createTodo

import android.widget.Button
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import com.example.doit.R

@BindingAdapter("setColor")
fun Button.setColor(item: LiveData<Boolean>?) {
    item?.let {
        if (!it.value!!) {
            setBackgroundColor(context.getColor(R.color.colorRed))
        } else {
            setBackgroundColor(context.getColor(R.color.primaryColor))
        }
    }
}