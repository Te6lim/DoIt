package com.te6lim.doit.todoList.categories

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.te6lim.doit.R

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