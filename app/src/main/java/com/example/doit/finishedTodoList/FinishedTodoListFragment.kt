package com.example.doit.finishedTodoList

import android.os.Bundle
import android.view.*
import android.widget.CheckBox
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.doit.*
import com.example.doit.database.CategoryDb
import com.example.doit.database.Todo
import com.example.doit.database.TodoDatabase
import com.example.doit.databinding.FragmentListTodoFinishedBinding
import com.example.doit.todoList.TodoListAdapter
import com.example.doit.todoList.TodoListFragment.Companion.DEF_KEY
import java.time.LocalDateTime

class FinishedTodoListFragment : Fragment(), ConfirmationCallbacks {

    companion object {
        const val ADDRESS = "finishedTodoList"
    }

    private lateinit var binding: FragmentListTodoFinishedBinding
    private lateinit var viewModel: FinishedTodoListViewModel
    private var menuItems: Menu? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_list_todo_finished, container, false
        )

        setHasOptionsMenu(true)

        val todoDatabase = TodoDatabase.getInstance(requireContext())
        val categoryDatabase = CategoryDb.getInstance(requireContext())
        val viewModelFactory = FinishedTodoListViewModelFactory(
            categoryDatabase.dao,
            todoDatabase.databaseDao,
        )
        viewModel = ViewModelProvider(
            this, viewModelFactory
        )[FinishedTodoListViewModel::class.java]

        binding.lifecycleOwner = this

        (requireActivity() as MainActivity).supportActionBar?.subtitle = null

        val adapter = TodoListAdapter(object : ActionCallback<Todo> {
            override fun onCheck(t: Todo, holder: View) {
                viewModel.updateTodo(
                    t.apply {
                        isFinished = holder.findViewById<CheckBox>(R.id.todo_check_box)!!.isChecked
                        dateFinished = if (isFinished) LocalDateTime.now()
                        else null
                    }
                )
            }

            override fun selectedView(position: Int, holder: View) {}
        })

        binding.completedTodoList.adapter = adapter

        with(viewModel) {

            awaitCategory.observe(viewLifecycleOwner) { }

            awaitTodoList.observe(viewLifecycleOwner) { }

            completedTodos.observe(viewLifecycleOwner) {
                it?.let {
                    adapter.submitList(it)
                }

                val item = menuItems?.findItem(R.id.clear)
                if (it.isNullOrEmpty()) {
                    if (item != null && item.isVisible)
                        item.isVisible = false
                } else {
                    if (item != null && !item.isVisible)
                        item.isVisible = true
                }
            }

            categoryCountPair.observe(viewLifecycleOwner) {
                (requireActivity() as MainActivity).supportActionBar?.subtitle =
                    requireContext().getString(R.string.category_plus_count, it.first, it.second)
            }

            navigating.observe(viewLifecycleOwner) { itIsTrue ->
                if (itIsTrue) {
                    (requireActivity() as MainActivity).supportActionBar?.subtitle = null
                    viewModel.isNavigating(false)
                }
            }
        }

        val handle = findNavController().currentBackStackEntry?.savedStateHandle
        handle?.getLiveData<Int>(DEF_KEY)?.observe(viewLifecycleOwner) { value ->
            if (value != null) {
                viewModel.emitCategory(value)
                handle.remove<Int>(DEF_KEY)
            }
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menuItems = menu
        inflater.inflate(R.menu.finished_todo_menu, menu)
        viewModel.completedTodos.value?.let {
            menu.findItem(R.id.clear).isVisible = it.isNotEmpty()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.clear -> {
                ConfirmationDialog(this).show(
                    (requireActivity()).supportFragmentManager, "C"
                )
                true
            }

            R.id.categoriesFragment -> {
                findNavController().navigate(
                    FinishedTodoListFragmentDirections
                        .actionFinishedTodoListFragmentToCategoriesFragment(ADDRESS)
                )
                viewModel.isNavigating(true)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun message(): String {
        return "clear finished Todos from ${viewModel.categoryCountPair.value!!.first}?"
    }

    override fun positiveAction() {
        viewModel.clearFinishedTodos()
    }
}