package com.example.doit.todoList

import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.doit.MainActivity
import com.example.doit.R
import com.example.doit.database.CategoryDb
import com.example.doit.database.TodoDatabase
import com.example.doit.databinding.FragmentListTodoBinding

class TodoListFragment : Fragment() {

    private lateinit var binding: FragmentListTodoBinding
    private lateinit var todoListViewModel: TodoListViewModel
    private var defaultCategoryId: Int = 0
    private lateinit var customActionMode: View

    private lateinit var mainActivity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_list_todo, container, false
        )
        mainActivity = (requireActivity() as MainActivity)

        setHasOptionsMenu(true)

        customActionMode = LayoutInflater.from(requireContext())
            .inflate(R.layout.contextual_actionbar, container, false)

        val callback = object : ActionMode.Callback {
            override fun onCreateActionMode(p0: ActionMode?, p1: Menu?): Boolean {

                return true
            }

            override fun onPrepareActionMode(p0: ActionMode?, p1: Menu?): Boolean {
                return true
            }

            override fun onActionItemClicked(p0: ActionMode?, p1: MenuItem?): Boolean {
                return false
            }

            override fun onDestroyActionMode(aM: ActionMode?) {
                todoListViewModel.contextActionBarEnabled(false)
            }

        }

        val todoDatabase = TodoDatabase.getInstance(requireContext())
        val categoryDatabase = CategoryDb.getInstance(requireContext())

        val viewModelFactory =
            TodoListViewModelFactory(categoryDatabase.dao, todoDatabase.databaseDao)
        todoListViewModel = ViewModelProvider(this, viewModelFactory)
            .get(TodoListViewModel::class.java)

        binding.lifecycleOwner = this

        val adapter = TodoListAdapter(CheckedTodoListener {
            todoListViewModel.delete(it)
        })

        binding.todoList.adapter = adapter

        binding.addNew.setOnClickListener {
            findNavController().navigate(
                TodoListFragmentDirections.actionTodoListFragmentToCreateTodoFragment(
                    defaultCategoryId
                )
            )
            todoListViewModel.isNavigating(true)
        }

        with(todoListViewModel) {
            categories.observe(viewLifecycleOwner) {}
            todoList.observe(viewLifecycleOwner) {}
            categoriesTransform.observe(viewLifecycleOwner) { list ->
                val spinner = customActionMode.findViewById<Spinner>(R.id.categorySelector)
                ArrayAdapter(
                    requireContext(), R.layout.category_item, list
                ).also { adapter ->
                    adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                    spinner.adapter = adapter
                }
            }
            defaultTransform.observe(viewLifecycleOwner) {}

            defaultCategory.observe(viewLifecycleOwner) { category ->
                defaultCategoryId = category!!.id
            }

            todoListByCategory.observe(viewLifecycleOwner) { list ->
                adapter.submitList(list)
            }

            isTodoListEmpty.observe(viewLifecycleOwner) {
                with(binding) {
                    if (it) {
                        todoList.visibility = View.GONE
                        emptyTodo.visibility = View.VISIBLE
                    } else {
                        todoList.visibility = View.VISIBLE
                        emptyTodo.visibility = View.GONE
                    }
                }
            }

            itemCountInCategory.observe(viewLifecycleOwner) {
                mainActivity.supportActionBar?.subtitle = resources
                    .getString(R.string.category_plus_count, it.first, it.second)
            }

            isNavigating.observe(viewLifecycleOwner) { navigating ->
                if (navigating) {
                    mainActivity.supportActionBar?.subtitle = null
                    todoListViewModel.isNavigating(false)
                }
            }
            contextActionBarEnabled.observe(viewLifecycleOwner) { enabled ->
                if (enabled) {
                    mainActivity.startActionMode(callback)
                        .apply {
                            this?.customView = customActionMode
                        }
                }
            }
        }

        val savedStateHandle = findNavController().currentBackStackEntry?.savedStateHandle
        savedStateHandle?.getLiveData<Int>("KEY")?.observe(viewLifecycleOwner) { value ->
            if (value != null) {
                todoListViewModel.emitDisplayCategoryAsDefault(value)
                savedStateHandle.remove<Int>("KEY")
            }
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        return inflater.inflate(R.menu.todo_list_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.categories -> {
                todoListViewModel.contextActionBarEnabled(true)
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }
}