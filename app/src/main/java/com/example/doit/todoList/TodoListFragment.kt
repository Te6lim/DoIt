package com.example.doit.todoList

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.CheckBox
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.view.ActionMode
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.RecyclerView
import com.example.doit.ActionCallback
import com.example.doit.MainActivity
import com.example.doit.R
import com.example.doit.database.CategoryDb
import com.example.doit.database.Todo
import com.example.doit.database.TodoDatabase
import com.example.doit.databinding.FragmentListTodoBinding
import java.time.LocalDateTime

class TodoListFragment : Fragment() {

    companion object {
        const val SCROLL = "SCROLL"
        const val DEF_KEY = "KEY"
    }

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

        val adapter = TodoListAdapter(object : ActionCallback<Todo> {

            override fun onCheck(t: Todo, holder: View) {

                todoListViewModel.updateTodo(
                    t.apply {
                        isFinished = holder.findViewById<CheckBox>(R.id.todo_check_box)!!.isChecked
                        dateFinished = if (isFinished) LocalDateTime.now()
                        else null
                    })
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun <H : RecyclerView.ViewHolder> onLongPress(
                position: Int, t: Todo, holder: View, adapter: RecyclerView.Adapter<H>
            ) {

                todoListViewModel.setItemSelected(position)
                if (!todoListViewModel.isLongPressed) {
                    todoListViewModel.isLongPressed = true
                    adapter.notifyDataSetChanged()
                } else {
                    val checkBox = holder.findViewById<CheckBox>(R.id.todo_check_box)
                    if (todoListViewModel.isLongPressed)
                        checkBox?.visibility = View.INVISIBLE
                    else checkBox?.visibility = View.VISIBLE
                }
            }

            override fun onClick(position: Int, t: Todo, holder: View) {
                todoListViewModel.clickAction(position)
                val checkBox = holder.findViewById<CheckBox>(R.id.todo_check_box)
                if (todoListViewModel.isLongPressed)
                    checkBox?.visibility = View.INVISIBLE
                else checkBox?.visibility = View.VISIBLE
            }

            override fun selectedView(position: Int, holder: View) {
                todoListViewModel.getItems()?.let {
                    switchBackground(todoListViewModel.getItems()!![position], holder)
                }
                if (todoListViewModel.isLongPressed) {
                    holder.findViewById<CheckBox>(R.id.todo_check_box).visibility = View.INVISIBLE
                }
            }
        })

        var actionMode: ActionMode? = null
        val actionModeCallback = object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                MenuInflater(requireContext()).inflate(R.menu.todo_action_mode_menu, menu)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                return when (item?.itemId) {
                    R.id.edit -> {
                        todoListViewModel.clickAction()
                        findNavController().navigate(
                            TodoListFragmentDirections.actionTodoListFragmentToCreateTodoFragment(
                                todoListViewModel.editTodo!!.catId
                            ).setTodoId(todoListViewModel.editTodo!!.todoId)
                        )
                        todoListViewModel.isNavigating(true)
                        true
                    }

                    R.id.done_all -> {
                        todoListViewModel.updatedSelected()
                        true
                    }

                    R.id.select_all -> {
                        todoListViewModel.selectAll()
                        item.isVisible = false
                        adapter.notifyDataSetChanged()
                        true
                    }

                    R.id.delete -> {
                        todoListViewModel.deleteSelected()
                        true
                    }
                    else -> false
                }
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onDestroyActionMode(mode: ActionMode?) {
                todoListViewModel.clickAction()
                todoListViewModel.isLongPressed = false
                adapter.notifyDataSetChanged()
            }
        }

        binding.todoList.adapter = adapter.also {
            it.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    if (savedInstanceState != null)
                        adapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy
                            .values()[savedInstanceState.getInt(SCROLL)]
                    else binding.todoList.scrollToPosition(0)
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

            itemsState.observe(viewLifecycleOwner) { list ->
                if (list.any { it }) setContextActionBarEnabled(true)
                else setContextActionBarEnabled(false)
            }

            contextActionBarEnabled.observe(viewLifecycleOwner) { isEnabled ->
                if (isEnabled) {
                    actionMode = mainActivity.startSupportActionMode(actionModeCallback)
                    binding.addNew.visibility = View.GONE
                } else {
                    actionMode?.let {
                        it.finish()
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

            selectionCount.observe(viewLifecycleOwner) { count ->
                actionMode?.let {
                    it.title = count.toString()
                    with(it.menu) {
                        findItem(R.id.select_all)?.isVisible = count != getItems()?.size
                        findItem(R.id.edit)?.isVisible = count == 1
                        findItem(R.id.done_all)?.isVisible = count > 1
                    }
                }
            }
        }

        val savedStateHandle = findNavController().currentBackStackEntry?.savedStateHandle
        savedStateHandle?.getLiveData<Int>(DEF_KEY)?.observe(viewLifecycleOwner) { value ->
            if (value != null) {
                todoListViewModel.emitDisplayCategoryAsDefault(value)
                savedStateHandle.remove<Int>(DEF_KEY)
            }
        }

        return binding.root
    }

    private fun switchBackground(value: Boolean, holder: View) {
        with(holder) {
            val checkBox = holder.findViewById<CheckBox>(R.id.todo_check_box)
            background = if (value) {
                checkBox?.visibility = View.INVISIBLE
                AppCompatResources.getDrawable(
                    context, R.drawable.item_selected_background
                )
            } else {
                checkBox?.visibility = View.VISIBLE
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
            R.id.categoriesFragment -> {
                NavigationUI.onNavDestinationSelected(item, findNavController())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(
            "SCROLL",
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY.ordinal
        )
        super.onSaveInstanceState(outState)
    }
}