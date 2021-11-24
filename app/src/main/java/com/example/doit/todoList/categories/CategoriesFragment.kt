package com.example.doit.todoList.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.doit.ActionCallback
import com.example.doit.MainActivity
import com.example.doit.R
import com.example.doit.database.CategoryDb
import com.example.doit.database.TodoDatabase
import com.example.doit.databinding.FragmentCategoriesBinding
import com.example.doit.todoList.TodoListFragment

class CategoriesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        val binding: FragmentCategoriesBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_categories, container, false
        )

        (requireActivity() as MainActivity).supportActionBar?.subtitle = null

        val todoDbDao = TodoDatabase.getInstance(requireContext()).databaseDao
        val categoryDbDao = CategoryDb.getInstance(requireContext()).dao
        val viewModelFactory = CategoriesViewModelFactory(categoryDbDao, todoDbDao)

        val viewModel = ViewModelProvider(
            this, viewModelFactory
        )[CategoriesViewModel::class.java]

        binding.lifecycleOwner = this

        val adapter = CategoriesAdapter(object : ActionCallback<CategoryInfo> {

            override fun onClick(position: Int, t: CategoryInfo, holder: View) {
                with(findNavController()) {
                    previousBackStackEntry?.savedStateHandle?.set(
                        TodoListFragment.DEF_KEY, t.id
                    )
                    popBackStack()
                }
            }

            override fun selectedView(position: Int, holder: View) {}

        })
        binding.categoriesRecyclerView.adapter = adapter

        viewModel.categoriesTransform.observe(viewLifecycleOwner) {}
        viewModel.todoListTransform.observe(viewLifecycleOwner) {}

        viewModel.catListInfo.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        return binding.root
    }
}