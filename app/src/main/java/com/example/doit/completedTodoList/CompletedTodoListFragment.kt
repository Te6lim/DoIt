package com.example.doit.completedTodoList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.doit.R
import com.example.doit.database.TodoDatabase
import com.example.doit.databinding.FragmentListTodoBinding
import com.example.doit.databinding.FragmentListTodoCompletedBinding
import com.example.doit.todoList.CheckedTodoListener
import com.example.doit.todoList.TodoListAdapter

class CompletedTodoListFragment : Fragment() {

    private lateinit var binding: FragmentListTodoCompletedBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_list_todo_completed, container, false
        )

        val todoDatabase = TodoDatabase.getInstance(requireContext())
        val viewModelFactory = CompletedTodoListViewModelFactory(todoDatabase.databaseDao)
        val viewModel = ViewModelProvider(this, viewModelFactory)
            .get(CompletedTodoListViewModel::class.java)

        binding.lifecycleOwner = this

        binding.completedTodoList.adapter = TodoListAdapter(CheckedTodoListener {
            viewModel.updateTodo(it)
        })

        return binding.root
    }
}