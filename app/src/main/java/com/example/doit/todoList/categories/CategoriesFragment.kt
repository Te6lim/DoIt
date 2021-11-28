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
import com.example.doit.todoList.toCategory

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
                position: Int, t: CategoryInfo, holder: View, adapter: RecyclerView.Adapter<H>
            ) {

                CategoryOptionsDialogFragment(object : CatDialogInterface {
                    override fun getTitle(): String {
                        return t.name
                    }

                    override fun onOptionClicked(option: Int) {

                        when (DialogOptions.values()[option]) {
                            DialogOptions.OPTION_A -> {
                                EditCategoryDialogFragment(
                                    object : EditCategoryDialogInterface {
                                        override fun currentCategoryName() = t.name

                                        override fun positiveAction(category: String) {
                                            viewModel.updateCategory(
                                                t.apply { name = category }.toCategory()
                                            )
                                        }

                                    }).show(
                                    (requireActivity().supportFragmentManager),
                                    "EDIT_DIALOG"
                                )
                            }

                            DialogOptions.OPTION_B -> {
                                if (t.isDefault) viewModel.clearCategory(t.toCategory())
                                else viewModel.changeDefault(t.id)
                            }

                            DialogOptions.OPTION_C -> {
                                viewModel.clearCategory(t.toCategory())
                            }

                            DialogOptions.OPTION_D -> {
                                viewModel.deleteCategory(t.toCategory())
                            }
                        }
                    }

                    override fun isItemDefault(): Boolean {
                        return t.isDefault
                    }

                }).show(mainActivity.supportFragmentManager, "CAT_DIALOG")
            }

            override fun selectedView(position: Int, holder: View) {}

        }).apply {
            registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                    binding.categoriesRecyclerView.scrollToPosition(0)
                }
            })
        }

        binding.categoriesRecyclerView.adapter = adapter

        with(viewModel) {
            categoriesTransform.observe(viewLifecycleOwner) {}

            todoListTransform.observe(viewLifecycleOwner) {}

            defaultCategory.observe(viewLifecycleOwner) {

            }

        }

        viewModel.catListInfo.observe(viewLifecycleOwner) {
            adapter.submitListWithHeaders(it)
        }

        return binding.root
    }
}