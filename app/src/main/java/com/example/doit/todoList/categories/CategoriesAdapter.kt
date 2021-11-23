package com.example.doit.todoList.categories

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.doit.R
import com.example.doit.databinding.ItemCategoryBinding

class CategoriesAdapter : ListAdapter<CategoryInfo, CategoryViewHolder>(CategoriesDiffCallBack()) {

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

    fun bind(category: CategoryInfo) {
        itemHolder.categoryInfo = category
    }
}

class CategoriesDiffCallBack : DiffUtil.ItemCallback<CategoryInfo>() {
    override fun areItemsTheSame(oldItem: CategoryInfo, newItem: CategoryInfo): Boolean {
        return false
    }

    override fun areContentsTheSame(oldItem: CategoryInfo, newItem: CategoryInfo): Boolean {
        return false
    }

}