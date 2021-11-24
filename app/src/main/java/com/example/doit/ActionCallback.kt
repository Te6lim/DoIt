package com.example.doit

import android.view.View
import com.example.doit.todoList.TodoListAdapter

interface ActionCallback<T> {

    fun onCheck(t: T, holder: View) {}

    fun onLongPress(position: Int, holder: View, adapter: TodoListAdapter) {}

    fun onClick(position: Int, t: T, holder: View) {}

    fun selectedView(position: Int, holder: View)
}