package com.example.doit.todoList

import android.os.Bundle
import android.view.*
import androidx.appcompat.view.ActionMode
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.doit.MainActivity
import com.example.doit.R
import com.example.doit.database.CategoryDb
import com.example.doit.database.Todo
import com.example.doit.database.TodoDatabase
import com.example.doit.databinding.FragmentListTodoBinding

open class TodoListFragment : Fragment() {

    private lateinit var binding: FragmentListTodoBinding
    private lateinit var todoListViewModel: TodoListViewModel

    private lateinit var mainActivity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_list_todo, container, false
        )
        mainActivity = (requireActivity() as MainActivity)

        setHasOptionsMenu(true)

        val todoDatabase = TodoDatabase.getInstance(requireContext())
        val categoryDatabase = CategoryDb.getInstance(requireContext())

        val viewModelFactory =
            TodoListViewModelFactory(categoryDatabase.dao, todoDatabase.databaseDao)
        todoListViewModel = ViewModelProvider(
            this, viewModelFactory
        )[TodoListViewModel::class.java]

        binding.lifecycleOwner = this

        val actionModeCallback = object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                MenuInflater(requireContext()).inflate(R.menu.todo_action_mode_menu, menu)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode?) {
                todoListViewModel.clickAction()
                mode?.finish()
            }
        }

        val adapter = TodoListAdapter(object : ActionCallback {
            override fun onCheck(todo: Todo) {
                todoListViewModel.updateTodo(todo)
            }

            override fun onLongPress(position: Int) {
                todoListViewModel.setItemSelected(position)
            }

            override fun onClick() {
                todoListViewModel.clickAction()
            }
        })

        binding.todoList.adapter = adapter.also {
            it.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {

                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    binding.todoList.scrollToPosition(0)
                }

            })
        }

        binding.addNew.setOnClickListener {
            findNavController().navigate(
                TodoListFragmentDirections.actionTodoListFragmentToCreateTodoFragment(
                    todoListViewModel.defaultCategory.value!!.id
                )
            )
            todoListViewModel.isNavigating(true)
        }

        with(todoListViewModel) {

            categoriesTransform.observe(viewLifecycleOwner) {}
            defaultTransform.observe(viewLifecycleOwner) {}
            items.observe(viewLifecycleOwner) {
                setContextActionBarEnabled(false)
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
                mainActivity.supportActionBar?.subtitle = resources.getString(
                    R.string.category_plus_count, it.first, it.second
                )
            }

            isNavigating.observe(viewLifecycleOwner) { navigating ->
                if (navigating) {
                    mainActivity.supportActionBar?.subtitle = null
                    todoListViewModel.isNavigating(false)
                }
            }

            var actionMode: ActionMode? = null
            contextActionBarEnabled.observe(viewLifecycleOwner) { isEnabled ->
                if (isEnabled) {
                    actionMode = mainActivity.startSupportActionMode(actionModeCallback)
                } else {
                    actionMode.let {
                        actionModeCallback.onDestroyActionMode(it)
                    }
                }
            }

            itemSelected.observe(viewLifecycleOwner) {
                todoListViewModel.setContextActionBarEnabled(it)
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
        return when (item.itemId) {
            R.id.categories -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}