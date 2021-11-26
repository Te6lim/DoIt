package com.example.doit

import android.view.View
import androidx.recyclerview.widget.RecyclerView

interface ActionCallback<T> {

    fun onCheck(t: T, holder: View) {}

    fun <H : RecyclerView.ViewHolder> onLongPress(
        position: Int, holder: View, adapter: RecyclerView.Adapter<H>
    ) {
    }

    fun onClick(position: Int, t: T, holder: View) {}

    fun selectedView(position: Int, holder: View)
}