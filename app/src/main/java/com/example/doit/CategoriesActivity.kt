package com.example.doit

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.doit.database.CategoryDb
import com.example.doit.database.TodoDatabase
import com.example.doit.databinding.ActivityCategoriesBinding
import com.example.doit.todoList.TodoListFragment
import com.example.doit.todoList.categories.*
import com.example.doit.todoList.toCategory

class CategoriesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)

        val binding: ActivityCategoriesBinding = DataBindingUtil.setContentView(
            this, R.layout.activity_categories
        )

        val todoDbDao = TodoDatabase.getInstance(this).databaseDao
        val categoryDbDao = CategoryDb.getInstance(this).dao
        val viewModelFactory = CategoriesViewModelFactory(categoryDbDao, todoDbDao)

        val viewModel = ViewModelProvider(
            this, viewModelFactory
        )[CategoriesViewModel::class.java]

        binding.lifecycleOwner = this

        val adapter = CategoriesAdapter(object : ActionCallback<CategoryInfo> {

            override fun onClick(position: Int, t: CategoryInfo, holder: View) {
                val intent = Intent()
                intent.apply { setResult(Activity.RESULT_OK) }
                    .putExtra(TodoListFragment.DEF_KEY, t.id)
                setResult(Activity.RESULT_OK, intent)
                /*with(findNavController()) {
                    if (t.todoCount != 0) {
                        previousBackStackEntry?.savedStateHandle?.set(
                            com.example.doit.todoList.TodoListFragment.DEF_KEY, t.id
                        )
                    }
                    popBackStack()
                }*/
                onBackPressed()
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
                                    (supportFragmentManager),
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
                                        supportFragmentManager, "CF"
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
                                    supportFragmentManager, "CF"
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
                                    supportFragmentManager, "CF"
                                )
                            }
                        }
                    }

                    override fun isItemDefault(): Boolean {
                        return t.isDefault
                    }

                }).show(supportFragmentManager, "CAT_DIALOG")
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
            categoriesTransform.observe(this@CategoriesActivity) {}

            todoListTransform.observe(this@CategoriesActivity) {}

            defaultCategory.observe(this@CategoriesActivity) {

            }

        }

        viewModel.catListInfo.observe(this) {
            adapter.submitListWithHeaders(it)
        }
    }
}