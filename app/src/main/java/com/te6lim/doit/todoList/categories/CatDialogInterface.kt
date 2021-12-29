package com.te6lim.doit.todoList.categories

interface CatDialogInterface {
    fun getTitle(): String

    fun onOptionClicked(option: Int)

    fun isItemDefault(): Boolean
}