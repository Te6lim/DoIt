package com.te6lim.doit.todoList.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.te6lim.doit.ActionCallback
import com.te6lim.doit.ConfirmationCallbacks
import com.te6lim.doit.ConfirmationDialog
import com.te6lim.doit.R
import com.te6lim.doit.database.CategoryDb
import com.te6lim.doit.database.TodoDatabase
import com.te6lim.doit.database.getInstance
import com.te6lim.doit.databinding.FragmentCategoriesBinding
import com.te6lim.doit.finishedTodoList.FinishedTodoListFragment
import com.te6lim.doit.todoList.TodoListFragment
import com.te6lim.doit.todoList.toCategory

class CategoriesFragment : Fragment() {

    companion object {
        const val LIST_STATE_KEY: String = "CategoriesFragment"
        const val DEF_KEY = "KEY"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        val binding: FragmentCategoriesBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_categories, container, false
        )

        val todoDbDao = TodoDatabase.getInstance(requireContext()).databaseDao
        val categoryDbDao = CategoryDb.getInstance(requireContext()).dao
        val viewModelFactory = CategoriesViewModelFactory(
            categoryDbDao, todoDbDao, getInstance(requireContext()).summaryDao
        )

        val viewModel = ViewModelProvider(
            this, viewModelFactory
        )[CategoriesViewModel::class.java]

        binding.lifecycleOwner = this

        val visitingFragmentAddress = CategoriesFragmentArgs.fromBundle(requireArguments()).visitor

        val adapter = CategoriesAdapter(object : ActionCallback<CategoryInfo> {

            override fun onClick(position: Int, t: CategoryInfo, holder: View) {
                with(findNavController()) {
                    when (visitingFragmentAddress) {
                        TodoListFragment.ADDRESS -> {
                            if (t.todoCount != 0) {
                                previousBackStackEntry?.savedStateHandle?.set(
                                    DEF_KEY, t.id
                                )
                                previousBackStackEntry?.savedStateHandle?.set(LIST_STATE_KEY, true)
                                popBackStack()
                            } else {
                                Toast.makeText(
                                    requireContext(), "Todos: 0", Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        FinishedTodoListFragment.ADDRESS -> {
                            if (t.todoCompletedCount != 0) {
                                previousBackStackEntry?.savedStateHandle?.set(
                                    DEF_KEY, t.id
                                )
                                popBackStack()
                            } else {
                                Toast.makeText(
                                    requireContext(), "Finished: 0", Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        else -> {
                        }
                    }
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
                                if (t.isDefault) {
                                    ConfirmationDialog(object : ConfirmationCallbacks {
                                        override fun message(): String {
                                            return "Clear all todos in ${t.name} ?"
                                        }

                                        override fun positiveAction() {
                                            viewModel.clearCategory(t.toCategory())
                                        }

                                    }).show(
                                        requireActivity().supportFragmentManager, "CF"
                                    )
                                } else viewModel.changeDefault(t.id)
                            }

                            DialogOptions.OPTION_C -> {
                                ConfirmationDialog(object : ConfirmationCallbacks {
                                    override fun message(): String {
                                        return "Clear all todos in ${t.name} ?"
                                    }

                                    override fun positiveAction() {
                                        viewModel.clearCategory(t.toCategory())
                                    }

                                }).show(
                                    requireActivity().supportFragmentManager, "CF"
                                )
                            }

                            DialogOptions.OPTION_D -> {
                                ConfirmationDialog(object : ConfirmationCallbacks {
                                    override fun message(): String {
                                        return "Delete ${t.name} ?"
                                    }

                                    override fun positiveAction() {
                                        viewModel.deleteCategory(t.toCategory())
                                    }

                                }).show(
                                    requireActivity().supportFragmentManager, "CF"
                                )
                            }
                        }
                    }

                    override fun isItemDefault(): Boolean {
                        return t.isDefault
                    }

                }).show(requireActivity().supportFragmentManager, "CAT_DIALOG")
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
            readySummary.observe(viewLifecycleOwner) {}

            categoriesTransform.observe(viewLifecycleOwner) {}

            todoListTransform.observe(viewLifecycleOwner) {}

            defaultCategory.observe(viewLifecycleOwner) {}

        }

        viewModel.catListInfo.observe(viewLifecycleOwner) {
            adapter.submitListWithHeaders(it)
        }

        return binding.root
    }
}