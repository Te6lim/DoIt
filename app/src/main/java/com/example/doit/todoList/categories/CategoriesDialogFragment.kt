package com.example.doit.todoList.categories

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import java.lang.IllegalStateException

class CategoriesDialogFragment(private val callBack: CatDialogInterface) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            AlertDialog.Builder(it)
                .setTitle(callBack.getTitle()).apply {
                    if (callBack.isItemDefault()) {
                        setItems(
                            arrayOf(
                                DialogOptions.OPTION_B.value,
                                DialogOptions.OPTION_C.value
                            )
                        ) { _, option ->

                        }
                    } else {
                        setItems(
                            arrayOf(
                                DialogOptions.OPTION_A.value, DialogOptions.OPTION_B.value,
                                DialogOptions.OPTION_C.value
                            )
                        ) { _, option ->

                        }
                    }
                }.create()

        } ?: throw IllegalStateException()
    }
}