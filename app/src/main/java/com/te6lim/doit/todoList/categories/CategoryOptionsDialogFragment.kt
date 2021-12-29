package com.te6lim.doit.todoList.categories

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.te6lim.doit.R
import com.te6lim.doit.databinding.LayoutCategoryEditBinding

class CategoryOptionsDialogFragment(
    private val callBack: CatDialogInterface
) : DialogFragment() {

    /* Find fix later */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val items = if (callBack.isItemDefault()) arrayOf(
            DialogOptions.OPTION_A.value,
            DialogOptions.OPTION_C.value
        )
        else arrayOf(
            DialogOptions.OPTION_A.value,
            DialogOptions.OPTION_B.value,
            DialogOptions.OPTION_C.value,
            DialogOptions.OPTION_D.value
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

class EditCategoryDialogFragment(
    private val callback: EditCategoryDialogInterface
) : DialogFragment() {

    /* Find fix later */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DataBindingUtil.inflate<LayoutCategoryEditBinding>(
            LayoutInflater.from(requireContext()),
            R.layout.layout_category_edit, null, false
        )

        with(binding) {
            categoryNameEdit.setText(callback.currentCategoryName())

            categoryNameEdit.addTextChangedListener {

            }

            confirmButton.setOnClickListener {
                callback.positiveAction(categoryNameEdit.text.toString())
                dismiss()
            }

            cancelButton.setOnClickListener {
                callback.negativeAction()
                dismiss()
            }
        }

        return activity?.let {
            AlertDialog.Builder(it)
                .setView(binding.root)
                .create()
        } ?: throw IllegalStateException()
    }
}