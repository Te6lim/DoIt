package com.example.doit.todoList.categories

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.doit.ActionCallback
import com.example.doit.R
import com.example.doit.databinding.ItemCategoryBinding
import com.example.doit.databinding.ItemHeaderCategoryBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ClassCastException

private const val ITEM_VIEW_TYPE_CAT = 0
private const val ITEM_VIEW_TYPE_HEADER = 1

class CategoriesAdapter(
    private val actionCallback: ActionCallback<CategoryInfo>,
) : ListAdapter<DataItem, RecyclerView.ViewHolder>(CategoriesDiffCallBack()) {

    private val adapterScope = CoroutineScope(Dispatchers.Main)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_CAT -> CategoryViewHolder.create(parent)
            ITEM_VIEW_TYPE_HEADER -> HeaderViewHolder.create(parent)
            else -> throw ClassCastException()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                holder.bind(getItem(position) as DataItem.Header)
            }
            is CategoryViewHolder -> {
                val catItem = getItem(position) as DataItem.CategoryItem
                holder.bind(catItem.catInfo, actionCallback)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.Header -> ITEM_VIEW_TYPE_HEADER
            is DataItem.CategoryItem -> ITEM_VIEW_TYPE_CAT
        }
    }

    fun submitListWithHeaders(list: List<CategoryInfo>) {
        adapterScope.launch {
            val catList: MutableList<DataItem> = (list.toMutableList()).map {
                DataItem.CategoryItem(it)
            }.toMutableList()

            val items: MutableList<DataItem> = mutableListOf(DataItem.Header(true))
            items.apply {
                addAll(catList)
            }
            items.add(2, DataItem.Header(false))

            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
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

    fun bind(category: CategoryInfo, actionCallback: ActionCallback<CategoryInfo>) {
        itemHolder.categoryInfo = category

        with(itemView) {
            setOnClickListener {
                actionCallback.onClick(absoluteAdapterPosition, category, itemView)
            }

            setOnLongClickListener {
                actionCallback.onLongPress(
                    absoluteAdapterPosition, itemView, bindingAdapter as CategoriesAdapter
                )
                true
            }
        }

        itemHolder.executePendingBindings()
    }
}

class HeaderViewHolder(
    private val itemHolder: ItemHeaderCategoryBinding
) : RecyclerView.ViewHolder(itemHolder.root) {
    companion object {
        fun create(parent: ViewGroup): HeaderViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val holder = DataBindingUtil.inflate<ItemHeaderCategoryBinding>(
                inflater, R.layout.item_header_category, parent, false
            )
            return HeaderViewHolder(holder)
        }
    }

    fun bind(item: DataItem.Header) {
        itemHolder.headerItem = item
        itemHolder.executePendingBindings()
    }
}

class CategoriesDiffCallBack : DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return false
    }

    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return false
    }

}

sealed class DataItem {
    abstract val id: Int

    class CategoryItem(val catInfo: CategoryInfo) : DataItem() {
        override val id = catInfo.id

    }

    class Header(val isDefault: Boolean) : DataItem() {
        override val id = Int.MIN_VALUE

    }
}