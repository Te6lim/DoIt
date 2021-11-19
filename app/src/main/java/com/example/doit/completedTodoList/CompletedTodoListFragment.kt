package com.example.doit.completedTodoList

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.doit.MainActivity
import com.example.doit.R
import com.example.doit.database.Todo
import com.example.doit.database.TodoDatabase
import com.example.doit.databinding.FragmentListTodoCompletedBinding
import com.example.doit.todoList.ActionCallback
import com.example.doit.todoList.TodoListAdapter

class CompletedTodoListFragment : Fragment() {

    private lateinit var binding: FragmentListTodoCompletedBinding
    private lateinit var viewModel: CompletedTodoListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_list_todo_completed, container, false
        )

        setHasOptionsMenu(true)

        val todoDatabase = TodoDatabase.getInstance(requireContext())
        val viewModelFactory = CompletedTodoListViewModelFactory(todoDatabase.databaseDao)
        viewModel = ViewModelProvider(
            this, viewModelFactory
        )[CompletedTodoListViewModel::class.java]

        binding.lifecycleOwner = this

        val adapter = TodoListAdapter(object : ActionCallback {
            override fun onCheck(todo: Todo) {
                viewModel.updateTodo(todo)
            }
        })

        binding.completedTodoList.adapter = adapter

        viewModel.completedTodos.observe(viewLifecycleOwner) {
            if (it.isEmpty()) setHasOptionsMenu(false)
            else setHasOptionsMenu(true)
            adapter.submitList(it)
        }

        viewModel.subtitleText.observe(viewLifecycleOwner) {
            (requireActivity() as MainActivity).supportActionBar?.subtitle = it.toString()
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.completed_todo_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.clear -> {
                viewModel.clearFinished()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}