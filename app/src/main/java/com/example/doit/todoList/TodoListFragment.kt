package com.example.doit.todoList

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.doit.R
import com.example.doit.database.TodoDatabase
import com.example.doit.databinding.FragmentListTodoBinding

class TodoListFragment : Fragment() {

    private lateinit var binding: FragmentListTodoBinding
    private lateinit var todoListViewModel: TodoListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_list_todo, container, false
        )

        setHasOptionsMenu(true)

        val database = TodoDatabase.getInstance(requireContext())

        val viewModelFactory = TodoListViewModelFactory(database.databaseDao)
        todoListViewModel = ViewModelProvider(this, viewModelFactory)
            .get(TodoListViewModel::class.java)

        binding.lifecycleOwner = this

        val adapter = TodoListAdapter()
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

            TodoListFragmentArgs.fromBundle(requireArguments()).todoInfo?.let {
                add(it)
                requireArguments().clear()
            }
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        return inflater.inflate(R.menu.add_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return NavigationUI.onNavDestinationSelected(item, findNavController()) ||
                super.onOptionsItemSelected(item)

    }
}