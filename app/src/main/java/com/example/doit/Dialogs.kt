package com.example.doit

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class ConfirmationDialog(act: ConfirmationCallbacks) : DialogFragment() {

    private val callbacks: ConfirmationCallbacks = act

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        retainInstance = true
        return activity?.let {
            AlertDialog.Builder(requireActivity())
                .setTitle("Confirmation")
                .setMessage(callbacks.message())
                .setPositiveButton("OK") { _, _ ->
                    callbacks.positiveAction()
                }
                .setNegativeButton("Cancel") { _, _ ->
                    callbacks.negativeAction()
                }.create()
        } ?: throw IllegalStateException()
    }

    override fun onDismiss(dialog: DialogInterface) {
        callbacks.negativeAction()
    }
}

interface ConfirmationCallbacks {

    fun message(): String

    fun positiveAction()

    fun negativeAction() {}
}