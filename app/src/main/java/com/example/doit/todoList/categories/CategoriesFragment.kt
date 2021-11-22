package com.example.doit.todoList.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.doit.MainActivity
import com.example.doit.R
import com.example.doit.databinding.FragmentCategoriesBinding

class CategoriesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding: FragmentCategoriesBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_categories, container, false
        )

        (requireActivity() as MainActivity).supportActionBar?.subtitle = null

        return binding.root
    }
}