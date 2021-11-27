package com.example.doit.todoList.categories

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import java.lang.IllegalStateException

class CategoriesDialogFragment(private val callBack: CatDialogInterface) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val items = if (callBack.isItemDefault()) arrayOf(
            DialogOptions.OPTION_B.value
        )
        else arrayOf(
            DialogOptions.OPTION_A.value,
            DialogOptions.OPTION_B.value,
            DialogOptions.OPTION_C.value
        )
        return activity?.let {
            AlertDialog.Builder(it)
                .setTitle(callBack.getTitle())
                .setItems(items) { _, option ->
                    callBack.onOptionClicked(option)
                }.create()

        } ?: throw IllegalStateException()
    }
}