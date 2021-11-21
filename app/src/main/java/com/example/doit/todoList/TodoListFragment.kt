package com.example.doit.todoList

import android.os.Bundle
import android.view.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.view.ActionMode
import androidx.core.view.get
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

        var actionMode: ActionMode? = null
        val actionModeCallback = object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                MenuInflater(requireContext()).inflate(R.menu.todo_action_mode_menu, menu)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                return when (item?.itemId) {
                    R.id.edit -> {
                        true
                    }

                    R.id.select_all -> {
                        todoListViewModel.selectAll()
                        item.isVisible = false
                        true
                    }

                    R.id.delete -> {
                        todoListViewModel.setToBeDeleted()
                        true
                    }
                    else -> false
                }
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

            override fun onClick(position: Int) {
                todoListViewModel.clickAction(position)
            }

            override fun selectedView(position: Int, holder: View) {
                switchBackground(todoListViewModel.items.value!![position], holder)
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

            todoList.observe(viewLifecycleOwner) { list ->
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

            items.observe(viewLifecycleOwner) { list ->
                if (list.any { it }) setContextActionBarEnabled(true)
                else setContextActionBarEnabled(false)
            }

            contextActionBarEnabled.observe(viewLifecycleOwner) { isEnabled ->
                if (isEnabled) {
                    actionMode = mainActivity.startSupportActionMode(actionModeCallback)
                    binding.addNew.visibility = View.GONE
                } else {
                    actionMode.let {
                        actionModeCallback.onDestroyActionMode(it)
                        binding.addNew.visibility = View.VISIBLE

                    }
                }

                mainActivity.mainViewModel.setContextActionbarActive(isEnabled)
            }

            viewHolderPosition.observe(viewLifecycleOwner) { position ->
                val holder = binding.todoList.findViewHolderForAdapterPosition(position)?.itemView
                contextActionBarEnabled.value?.let { _ ->
                    holder?.let {
                        switchBackground(todoListViewModel.getItems()!![position], holder)
                    }
                }
            }

            selectionCount.observe(viewLifecycleOwner) {
                actionMode?.title = getString(R.string.selection_count, it)
                actionMode?.menu?.get(0)?.isVisible = it == 1
            }

            toBeDeleted.observe(viewLifecycleOwner) {
                deleteSelected(it)
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

    private fun switchBackground(value: Boolean, holder: View) {
        with(holder) {
            background = if (value) {
                AppCompatResources.getDrawable(
                    context, R.drawable.item_selected_background
                )
            } else {
                AppCompatResources.getDrawable(
                    context, R.drawable.rounded_background
                )
            }
        }
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