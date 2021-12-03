package com.example.doit

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import java.lang.IllegalStateException

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

                }.create()
        } ?: throw IllegalStateException()
    }
}

interface ConfirmationCallbacks {

    fun message(): String

    fun positiveAction()
}