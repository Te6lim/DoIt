package com.example.doit.todoList.createTodo

import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import com.example.doit.R
import com.example.doit.database.Todo

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

@BindingAdapter("actionText")
fun Button.actionText(value: Todo?) {
    text = if (value != null) context.getString(R.string.update)
    else context.getString(R.string.create)
}