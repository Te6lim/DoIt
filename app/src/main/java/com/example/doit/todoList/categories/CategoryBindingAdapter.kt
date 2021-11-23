package com.example.doit.todoList.categories

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.doit.R

@BindingAdapter("setCatName")
fun TextView.setCatName(catInfo: CategoryInfo?) {
    catInfo?.let {
        text = catInfo.name
    }
}

@BindingAdapter("setCatTodoCount")
fun TextView.setCatTodoCount(catInfo: CategoryInfo?) {
    catInfo?.let {
        text = context.getString(R.string.todo_count, catInfo.todoCount)
    }
}

@BindingAdapter("setCatTodoCompletedCount")
fun TextView.setCatTodoCompletedCount(catInfo: CategoryInfo?) {
    catInfo?.let {
        text = context.getString(R.string.finished_todo_count, catInfo.todoCompletedCount)
    }
}