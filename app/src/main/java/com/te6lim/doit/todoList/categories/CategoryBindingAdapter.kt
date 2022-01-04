package com.te6lim.doit.todoList.categories

import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import com.te6lim.doit.R
import com.te6lim.doit.todoList.toCategory

@BindingAdapter("setCatName")
fun TextView.setCatName(catInfo: CategoryInfo?) {
    catInfo?.let {
        text = catInfo.name
    }
}

@BindingAdapter("setCatTodoCount")
fun TextView.setCatTodoCount(catInfo: CategoryInfo?) {
    catInfo?.let {
        text = catInfo.todoCount.toString()
    }
}

@BindingAdapter("setCatTodoCompletedCount")
fun TextView.setCatTodoCompletedCount(catInfo: CategoryInfo?) {
    catInfo?.let {
        text = catInfo.todoCompletedCount.toString()
    }
}

@BindingAdapter("setHeaderText")
fun TextView.setHeaderText(dataItem: DataItem.Header?) {
    dataItem?.let {
        text = if (it.isDefault) context.getString(R.string.default_header)
        else context.getString(R.string.others_header)
    }
}

@BindingAdapter("setBadgeVisibility")
fun ConstraintLayout.setBadgeVisibility(catInfo: CategoryInfo?) {
    catInfo?.let {
        val lateTodosCount = it.toCategory().lateTodos
        visibility = if (lateTodosCount > 0) {
            findViewById<TextView>(R.id.indicator_text).text = when {
                lateTodosCount in 1..9 -> {
                    lateTodosCount.toString()
                }
                lateTodosCount > 9 -> "9+"
                else -> lateTodosCount.toString()
            }
            View.VISIBLE
        } else View.GONE

    }
}