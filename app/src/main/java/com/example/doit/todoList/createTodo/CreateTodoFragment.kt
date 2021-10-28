package com.example.doit.todoList.createTodo

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.RadioButton
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.doit.R
import com.example.doit.database.Category
import com.example.doit.database.CategoryDb
import com.example.doit.database.TodoDatabase
import com.example.doit.databinding.FragmentTodoCreateBinding
import java.time.LocalTime
import java.util.*

class CreateTodoFragment : Fragment() {

    private lateinit var binding: FragmentTodoCreateBinding
    private lateinit var viewModel: CreateTodoViewModel
    private lateinit var actionbar: ActionBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_todo_create, container, false
        )

        actionbar = (requireActivity() as AppCompatActivity).supportActionBar!!

        val categoryDb = CategoryDb.getInstance(requireContext())
        val todoDb = TodoDatabase.getInstance(requireContext())

        val viewModelFactory = CreateTodoViewModelFactory(todoDb.databaseDao, categoryDb.dao)
        viewModel = ViewModelProvider(
            this, viewModelFactory
        ).get(CreateTodoViewModel::class.java)

        binding.lifecycleOwner = this

        binding.viewModel = viewModel

        with(viewModel) {
            categories.observe(viewLifecycleOwner) {
                if (it.isNullOrEmpty()) initializeCategories()
                initializeDefault()
            }

            defaultCategory.observe(viewLifecycleOwner) { category ->
                setTitleToDefaultCategoryName(category)
                addCategoryViews(categories.value!!)
            }

            todoInfo.observe(viewLifecycleOwner) {
                if (it.todoValid()) {
                    viewModel.add(it)
                    findNavController().navigate(
                        CreateTodoFragmentDirections
                            .actionCreateTodoFragmentToTodoListFragment()
                    )
                }
            }

            categoryEditTextIsOpen.observe(viewLifecycleOwner) { isOpen ->
                if (isOpen) {
                    with(binding) {
                        headerTextCategory.visibility = View.GONE
                        categoryEditText.visibility = View.VISIBLE
                        if (categoryEditText.text.isNullOrEmpty())
                            addCategoryButton.setImageResource(R.drawable.ic_close)
                        else addCategoryButton.setImageResource(R.drawable.ic_done)
                    }
                } else {
                    val categoryString = binding.categoryEditText.text.toString()

                    if (categoryString.isNotEmpty()) {
                        viewModel.addNewCategory(categoryString)
                        binding.categoryEditText.text.clear()
                    }

                    with(binding) {
                        headerTextCategory.visibility = View.VISIBLE
                        categoryEditText.visibility = View.GONE
                        addCategoryButton.setImageResource(R.drawable.ic_add)
                    }
                }
            }
        }

        binding.categoryEditText.addTextChangedListener {
            with(binding) {
                if (categoryEditText.isVisible) {
                    if (it.isNullOrEmpty()) addCategoryButton.setImageResource(R.drawable.ic_close)
                    else addCategoryButton.setImageResource(R.drawable.ic_done)
                }
            }

        }

        binding.todoDescription.addTextChangedListener {
            viewModel.todo.setDescription(it.toString())
        }

        binding.deadlineSwitch.setOnCheckedChangeListener { _, isOn ->
            binding.deadlineButton.isEnabled = isOn
            viewModel.todo.setIsDeadlineEnabled(isOn)
        }

        binding.deadlineButton.setOnClickListener {
            val h = LocalTime.now().hour
            val mi = LocalTime.now().minute

            TimePickerDialog(requireContext(), { _, hour, min ->
                viewModel.todo.setDeadlineTime(hour, min)
            }, h, mi, false).show()

            val y = Calendar.getInstance().get(Calendar.YEAR)
            val m = Calendar.getInstance().get(Calendar.MONTH)
            val d = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(requireContext(), { _, year, month, day ->
                viewModel.todo.setDeadlineDate(year, month + 1, day)
            }, y, m, d).show()
        }

        binding.dateButton.setOnClickListener {
            val y = Calendar.getInstance().get(Calendar.YEAR)
            val m = Calendar.getInstance().get(Calendar.MONTH)
            val d = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(requireContext(), { _, year, month, day ->
                viewModel.todo.setDate(year, month + 1, day)
            }, y, m, d).show()
        }

        binding.timeButton.setOnClickListener {
            val h = LocalTime.now().hour
            val m = LocalTime.now().minute
            TimePickerDialog(context, { _, hourOfDay, minute ->
                viewModel.todo.setTime(hourOfDay, minute)
            }, h, m, false).show()
        }

        binding.categorySelection.setOnCheckedChangeListener { _, id ->
            with(viewModel) {
                getCategoryById(id)?.let {
                    todo.setCategory(it)
                }
            }
        }

        with(binding) {
            addCategoryButton.setOnClickListener {
                if (categoryEditText.visibility == View.GONE)
                    viewModel!!.makeCategoryEditTextVisible()
                else viewModel!!.makeCategoryEditTextNotVisible()
            }
        }

        return binding.root
    }

    private fun setTitleToDefaultCategoryName(category: Category) {
        (requireActivity() as AppCompatActivity).supportActionBar!!.title = category.name
    }

    private fun addCategoryViews(categories: List<Category>) {
        binding.categorySelection.removeAllViews()
        for (cat in categories) {
            binding.categorySelection.addView(
                RadioButton(requireContext()).apply {
                    id = cat.id
                    text = cat.name
                    if (viewModel.isDefault(id)) isChecked = true
                }
            )
        }
    }
}