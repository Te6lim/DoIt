package com.example.doit.finishedTodoList

import android.os.Bundle
import android.view.*
import android.widget.CheckBox
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.doit.ActionCallback
import com.example.doit.MainActivity
import com.example.doit.R
import com.example.doit.database.Todo
import com.example.doit.database.TodoDatabase
import com.example.doit.databinding.FragmentListTodoCompletedBinding
import com.example.doit.todoList.TodoListAdapter
import java.time.LocalDateTime

class FinishedTodoListFragment : Fragment() {

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
        val viewModelFactory = FinishedTodoListViewModelFactory(todoDatabase.databaseDao)
        viewModel = ViewModelProvider(
            this, viewModelFactory
        )[CompletedTodoListViewModel::class.java]

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

        viewModel.completedTodos.observe(viewLifecycleOwner) {
            it?.let {
                if (it.isEmpty()) setHasOptionsMenu(false)
                else setHasOptionsMenu(true)
                adapter.submitList(it)
            }
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