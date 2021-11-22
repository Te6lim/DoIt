package com.example.doit.todoList.categories

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.doit.R
import com.example.doit.database.Category
import com.example.doit.databinding.ItemCategoryBinding

class CategoriesAdapter : ListAdapter<Category, CategoryViewHolder>(CategoriesDiffCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}

class CategoryViewHolder(
    private val itemHolder: ItemCategoryBinding
) : RecyclerView.ViewHolder(itemHolder.root) {

    companion object {
        fun create(parent: ViewGroup): CategoryViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val holder = DataBindingUtil.inflate<ItemCategoryBinding>(
                inflater, R.layout.item_category, parent, false
            )
            return CategoryViewHolder(holder)
        }
    }

    fun bind(category: Category) {

    }
}

class CategoriesDiffCallBack : DiffUtil.ItemCallback<Category>() {
    override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
        return false
    }

    override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
        return false
    }

}