package com.example.doit.todoList.categories

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.doit.ActionCallback
import com.example.doit.MainActivity
import com.example.doit.R
import com.example.doit.database.CategoryDb
import com.example.doit.database.TodoDatabase
import com.example.doit.databinding.FragmentCategoriesBinding
import com.example.doit.todoList.TodoListFragment

class CategoriesFragment : Fragment() {

    private lateinit var mainActivity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        val binding: FragmentCategoriesBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_categories, container, false
        )

        mainActivity = (requireActivity() as MainActivity).apply {
            supportActionBar?.subtitle = null
        }

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

            override fun <H : RecyclerView.ViewHolder> onLongPress(
                position: Int, holder: View, adapter: RecyclerView.Adapter<H>
            ) {

                val realPosition = if (position < 2) position - 1
                else position - 2

                CategoriesDialogFragment(object : CatDialogInterface {
                    override fun getTitle(): String {
                        return viewModel.categoriesList()[realPosition].name
                    }

                    override fun onOptionClicked(option: Int) {

                        when (DialogOptions.values()[option]) {
                            DialogOptions.OPTION_A -> {

                            }

                            DialogOptions.OPTION_B -> {

                            }

                            DialogOptions.OPTION_C -> {

                            }
                        }
                    }

                    override fun isItemDefault(): Boolean {
                        return viewModel.categoriesList()[realPosition].isDefault
                    }

                }).show(mainActivity.supportFragmentManager, "CAT_DIALOG")
            }

            override fun selectedView(position: Int, holder: View) {}

        })
        binding.categoriesRecyclerView.adapter = adapter

        with(viewModel) {
            categoriesTransform.observe(viewLifecycleOwner) {}

            todoListTransform.observe(viewLifecycleOwner) {}

        }

        viewModel.catListInfo.observe(viewLifecycleOwner) {
            adapter.submitListWithHeaders(it)
        }

        return binding.root
    }
}