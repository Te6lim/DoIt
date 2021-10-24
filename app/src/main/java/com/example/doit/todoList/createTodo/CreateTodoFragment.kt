package com.example.doit.todoList.createTodo

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.RadioButton
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.doit.R
import com.example.doit.database.Category
import com.example.doit.database.CategoryDb
import com.example.doit.databinding.FragmentTodoCreateBinding
import java.time.LocalDate
import java.time.LocalTime

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
        val viewModelFactory = CreateTodoViewModelFactory(categoryDb.dao)
        viewModel = ViewModelProvider(
            this, viewModelFactory
        ).get(CreateTodoViewModel::class.java)

        binding.lifecycleOwner = this

        binding.viewModel = viewModel

        with (viewModel) {
            categories.observe(viewLifecycleOwner) { list ->
                if (list.isNullOrEmpty()) {
                    initializeCategories()
                }
                initializeDefault()
            }

            defaultCategory.observe(viewLifecycleOwner) {
                it?.let{ category ->
                    setTitleToDefaultCategoryName(category)
                    addCategoryViews(categories.value!!)
                }
            }

            todoInfo.observe(viewLifecycleOwner) {
                if (it.isValid()) {
                    findNavController().navigate(
                        CreateTodoFragmentDirections
                            .actionCreateTodoFragmentToTodoListFragment().setTodoInfo(it)
                    )
                    clearTodoInfo()
                }
            }
        }

        binding.todoDescription.addTextChangedListener {
            viewModel.todo.description = it.toString()
        }

        binding.deadlineSwitch.setOnCheckedChangeListener { _, isChecked ->
            binding.deadlineButton.isEnabled = isChecked
        }

        binding.dateButton.setOnClickListener {
            val y = LocalDate.now().year
            val m = LocalDate.now().month.value
            val d = LocalDate.now().dayOfMonth
            DatePickerDialog(requireContext(),
                { _, year, month, day ->
                    viewModel.todo.setDate(year, month, day)
                }, y, m, d).show()
        }

        binding.timeButton.setOnClickListener {
            val hourOfDay = LocalTime.now().hour
            val minute = LocalTime.now().minute
            TimePickerDialog(context, { _, h, m ->
                viewModel.todo.setTime(h, m)
            }, hourOfDay, minute, false).show()
        }

        binding.categorySelection.setOnCheckedChangeListener { _, id ->
            with(viewModel) {
                getCategoryById(id)?.let {
                    todo.setCategory(it)
                }
            }
        }

        binding.addCategoryButton.setOnClickListener {
            if (binding.categoryEditText.visibility == View.GONE) {
                binding.headerTextCategory.visibility = View.GONE
                binding.categoryEditText.visibility = View.VISIBLE
                binding.addCategoryButton.setImageResource(R.drawable.ic_done)
            } else {
                val categoryString = binding.categoryEditText.text.toString()
                if (categoryString.isNotEmpty()) {
                    viewModel.addNewCategory(categoryString)
                }
                binding.headerTextCategory.visibility = View.VISIBLE
                binding.categoryEditText.visibility = View.GONE
                binding.addCategoryButton.setImageResource(R.drawable.ic_add)
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