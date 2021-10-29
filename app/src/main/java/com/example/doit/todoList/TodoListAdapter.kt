package com.example.doit.todoList

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.doit.R
import com.example.doit.database.Todo
import com.example.doit.databinding.ItemTodoBinding

class TodoListAdapter(private val listener: CheckedTodoListener) :
    ListAdapter<Todo, TodoListAdapter.TodoViewHolder>(TodoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        return TodoViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val todoItem = getItem(position)
        holder.bind(todoItem, listener)
    }

    class TodoViewHolder(
        private val itemViewBinding: ItemTodoBinding
    ) : RecyclerView.ViewHolder(itemViewBinding.root) {

        companion object {
            fun create(parent: ViewGroup): TodoViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val itemViewBinding: ItemTodoBinding = DataBindingUtil.inflate(
                    layoutInflater, R.layout.item_todo, parent, false
                )
                return TodoViewHolder(itemViewBinding)
            }
        }

        fun bind(todo: Todo, checkListener: CheckedTodoListener) {
            with(itemViewBinding) {
                todoItem = todo
                itemViewBinding.todoItemView.setOnLongClickListener {
                    true
                }
                itemListener = checkListener
                executePendingBindings()
            }
        }
    }
}

class TodoDiffCallback : DiffUtil.ItemCallback<Todo>() {
    override fun areItemsTheSame(oldItem: Todo, newItem: Todo): Boolean {
        return oldItem.todoId == newItem.todoId
    }

    override fun areContentsTheSame(oldItem: Todo, newItem: Todo): Boolean {
        return oldItem == newItem
    }

}

class CheckedTodoListener(private val x: (Long) -> Unit) {
    fun onChecked(itemId: Long) {
        x(itemId)
    }
}