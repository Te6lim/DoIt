package com.te6lim.doit.todoList

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.te6lim.doit.ActionCallback
import com.te6lim.doit.R
import com.te6lim.doit.database.Todo
import com.te6lim.doit.databinding.ItemTodoBinding

class TodoListAdapter(private val callback: ActionCallback<Todo>) :
    ListAdapter<Todo, TodoViewHolder>(TodoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        return TodoViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val todoItem = getItem(position)
        holder.bind(todoItem, callback)
    }
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

    fun bind(todo: Todo, callback: ActionCallback<Todo>) {
        with(itemViewBinding) {
            todoItem = todo

            todoCheckBox.isChecked = todo.isFinished

            todoCheckBox.setOnClickListener {
                callback.onCheck(todo, itemView)
            }

            itemView.setOnLongClickListener {
                callback.onLongPress(
                    absoluteAdapterPosition, todo, itemView, bindingAdapter as TodoListAdapter
                )
                true
            }

            itemView.setOnClickListener {
                callback.onClick(absoluteAdapterPosition, todo, itemView)
            }

            callback.selectedView(absoluteAdapterPosition, itemView)

            executePendingBindings()
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