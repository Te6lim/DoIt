package com.example.doit.todoList

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.doit.R
import com.example.doit.database.CategoryDb
import com.example.doit.database.TodoDatabase
import com.example.doit.databinding.FragmentListTodoBinding

class TodoListFragment : Fragment() {

    private lateinit var binding: FragmentListTodoBinding
    private lateinit var todoListViewModel: TodoListViewModel
    private var defaultCategoryId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_list_todo, container, false
        )

        setHasOptionsMenu(true)

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

        with(todoListViewModel) {
            todoList.observe(viewLifecycleOwner) {
                adapter.submitList(it)
            }

            isTodoListEmpty.observe(viewLifecycleOwner) {
                if (it) {
                    binding.todoList.visibility = View.GONE
                    binding.emptyTodo.visibility = View.VISIBLE
                } else {
                    binding.todoList.visibility = View.VISIBLE
                    binding.emptyTodo.visibility = View.GONE
                }
            }

            categories.observe(viewLifecycleOwner) {
                if (it.isNullOrEmpty()) {
                    initializeCategories()
                }
                initializeDefault()
            }

            defaultCategory.observe(viewLifecycleOwner) {
                defaultCategoryId = it.id
            }
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        return inflater.inflate(R.menu.add_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.createTodoFragment -> {
                findNavController().navigate(
                    TodoListFragmentDirections.actionTodoListFragmentToCreateTodoFragment(
                        defaultCategoryId
                    )
                )
                true
            }
            else -> {
                NavigationUI.onNavDestinationSelected(item, findNavController()) ||
                        super.onOptionsItemSelected(item)
            }
        }
    }
}