package com.te6lim.doit.todoList.categories

interface EditCategoryDialogInterface {
    fun currentCategoryName(): String

    fun positiveAction(category: String)

    fun negativeAction() {

    }
}