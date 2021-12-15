package com.example.doit.todoList.createTodo

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.RadioButton
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
import com.example.doit.database.getInstance
import com.example.doit.databinding.FragmentTodoCreateBinding
import com.example.doit.summary.SummaryViewModel
import com.example.doit.summary.SummaryViewModelFactory
import java.time.LocalTime
import java.util.*

class CreateTodoFragment : Fragment() {

    private lateinit var binding: FragmentTodoCreateBinding
    private lateinit var viewModel: CreateTodoViewModel

    private lateinit var summaryViewModel: SummaryViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_todo_create, container, false
        )

        val catDb = CategoryDb.getInstance(requireContext()).dao
        val todoDb = TodoDatabase.getInstance(requireContext()).databaseDao

        binding.lifecycleOwner = this

        val viewModelFactory = CreateTodoViewModelFactory(
            todoDb, catDb,
            CreateTodoFragmentArgs.fromBundle(requireArguments()).todoId
        )

        viewModel = ViewModelProvider(
            this, viewModelFactory
        )[CreateTodoViewModel::class.java]

        summaryViewModel = ViewModelProvider(
            requireActivity(), SummaryViewModelFactory(getInstance(requireActivity()).summaryDao)
        )[SummaryViewModel::class.java]

        binding.viewModel = viewModel

        val categoryId = CreateTodoFragmentArgs.fromBundle(requireArguments()).defaultCategoryId

        with(viewModel) {
            categories.observe(viewLifecycleOwner) {
                when (binding.categorySelection.childCount) {
                    0 -> {
                        addCategoryViews(categories.value!!)
                        if (categoryId != -1) emitCategory(categoryId)
                    }

                    else -> binding.categorySelection.addView(
                        RadioButton(requireContext()).apply {
                            id = it[it.size - 1].id
                            text = it[it.size - 1].name
                            this@CreateTodoFragment.viewModel.emitCategory(id)
                        }, 0
                    )
                }
            }

            todoCreated.observe(viewLifecycleOwner) { isCreated ->
                if (isCreated) {
                    findNavController().apply {
                        val id = binding.categorySelection.checkedRadioButtonId
                        previousBackStackEntry?.savedStateHandle?.set("KEY", id)
                        summaryViewModel.updateCreatedCount()
                    }.popBackStack()
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

            category.observe(viewLifecycleOwner) {
                it?.let {
                    setTitleToDefaultCategoryName(it)
                    binding.categorySelection.findViewById<RadioButton>(it.id)?.isChecked = true
                }
            }

            editTodo.observe(viewLifecycleOwner) { }
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
            viewModel.todoModel.setDescription(it.toString())
        }

        binding.deadlineSwitch.setOnCheckedChangeListener { _, isOn ->
            binding.deadlineButton.isEnabled = isOn
            viewModel.todoModel.setHasDeadlineEnabled(isOn)
        }

        binding.deadlineButton.setOnClickListener {

            val y = Calendar.getInstance().get(Calendar.YEAR)
            val m = Calendar.getInstance().get(Calendar.MONTH)
            val d = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(requireContext(), { _, year, month, day ->
                viewModel.todoModel.setDeadlineDate(year, month + 1, day)

                val h = LocalTime.now().hour
                val mi = LocalTime.now().minute

                TimePickerDialog(requireContext(), { _, hour, min ->
                    viewModel.todoModel.setDeadlineTime(hour, min)
                }, h, mi, false).show()

            }, y, m, d).show()
        }

        binding.dateButton.setOnClickListener {
            val y = Calendar.getInstance().get(Calendar.YEAR)
            val m = Calendar.getInstance().get(Calendar.MONTH)
            val d = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(requireContext(), { _, year, month, day ->
                viewModel.todoModel.setDateTodo(year, month + 1, day)
            }, y, m, d).show()
        }

        binding.timeButton.setOnClickListener {
            val h = LocalTime.now().hour
            val m = LocalTime.now().minute
            TimePickerDialog(context, { _, hourOfDay, minute ->
                viewModel.todoModel.setTimeTodo(hourOfDay, minute)
            }, h, m, false).show()
        }

        binding.categorySelection.setOnCheckedChangeListener { _, id ->
            viewModel.emitCategory(id)
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
        for (cat in categories) {
            binding.categorySelection.addView(
                RadioButton(requireContext()).apply {
                    id = cat.id
                    text = cat.name
                }
            )
        }
    }
}