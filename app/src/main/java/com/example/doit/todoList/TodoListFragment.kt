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
import androidx.recyclerview.widget.RecyclerView
import com.example.doit.*
import com.example.doit.database.*
import com.example.doit.databinding.FragmentListTodoBinding
import com.example.doit.todoList.categories.CategoriesFragment.Companion.DEF_KEY
import com.example.doit.todoList.categories.CategoriesFragment.Companion.LIST_STATE_KEY
import java.time.LocalDateTime

class TodoListFragment : Fragment(), ConfirmationCallbacks {

    companion object {
        const val SCROLL = "SCROLL"
        const val ADDRESS = "Todolist"
    }

    private lateinit var binding: FragmentListTodoBinding
    private lateinit var todoListViewModel: TodoListViewModel

    private lateinit var mainActivity: MainActivity

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_list_todo, container, false
        )
        mainActivity = (requireActivity() as MainActivity)

        setHasOptionsMenu(true)

        val todoDb = TodoDatabase.getInstance(requireContext()).databaseDao
        val catDb = CategoryDb.getInstance(requireContext()).dao

        val viewModelFactory =
            TodoListViewModelFactory(catDb, todoDb, getInstance(requireContext()).summaryDao)
        todoListViewModel = ViewModelProvider(
            this, viewModelFactory
        )[TodoListViewModel::class.java]

        binding.lifecycleOwner = this

        val adapter = TodoListAdapter(object : ActionCallback<Todo> {

            override fun onCheck(t: Todo, holder: View) {
                val checkBox = holder.findViewById<CheckBox>(R.id.todo_check_box)
                todoListViewModel.updateTodo(
                    t.apply {
                        isFinished = checkBox!!.isChecked
                        dateFinished = if (isFinished) LocalDateTime.now()
                        else null
                    })
            }

            var isSelected: Boolean = false

            override fun <H : RecyclerView.ViewHolder> onLongPress(
                position: Int, t: Todo, holder: View, adapter: RecyclerView.Adapter<H>
            ) {

                isSelected = todoListViewModel.interact(position, true)
                if (todoListViewModel.longPressStatusChanged) {
                    adapter.notifyDataSetChanged()
                    todoListViewModel.setLongPressedStatusChanged(false)
                } else switchBackground(isSelected, holder)
            }

            override fun onClick(position: Int, t: Todo, holder: View) {
                isSelected = todoListViewModel.interact(position, false)
                if (todoListViewModel.longPressStatusChanged) {
                    binding.todoList.adapter!!.notifyDataSetChanged()
                    todoListViewModel.setLongPressedStatusChanged(false)
                } else switchBackground(isSelected, holder)
            }

            override fun selectedView(position: Int, holder: View) {
                val checkBox = holder.findViewById<CheckBox>(R.id.todo_check_box)
                if (todoListViewModel.isLongPressed.value!!)
                    checkBox?.visibility = View.INVISIBLE
                else checkBox?.visibility = View.VISIBLE

                switchBackground(todoListViewModel.itemsState()[position], holder)
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

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                return when (item?.itemId) {
                    R.id.edit -> {
                        findNavController().navigate(
                            TodoListFragmentDirections.actionTodoListFragmentToCreateTodoFragment(
                            ).setEditTodoId(todoListViewModel.editTodo!!.todoId)
                                .setActiveCategoryId(todoListViewModel.activeCategory.value!!.id)
                        )
                        todoListViewModel.interact()
                        todoListViewModel.isNavigating(true)
                        true
                    }

                    R.id.done_all -> {
                        todoListViewModel.updatedSelected()
                        todoListViewModel.interact()
                        true
                    }

                    R.id.select_all -> {
                        if (todoListViewModel.selectionCount.value != 0)
                            todoListViewModel.selectAll(true)
                        else {
                            todoListViewModel.selectAll(false)
                            todoListViewModel.interact()
                        }
                        adapter.notifyDataSetChanged()
                        true
                    }

                    R.id.delete -> {
                        ConfirmationDialog(this@TodoListFragment).show(
                            mainActivity.supportFragmentManager, "T"
                        )
                        true
                    }
                    else -> false
                }
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onDestroyActionMode(mode: ActionMode?) {
                todoListViewModel.interact()
                adapter.notifyDataSetChanged()
            }
        }

        binding.todoList.adapter = adapter.also {
            it.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    if (savedInstanceState != null)
                        adapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy
                            .values()[savedInstanceState.getInt(SCROLL)]
                    else binding.todoList.scrollToPosition(positionStart)
                }
            })
        }

        binding.addNew.setOnClickListener {
            findNavController().navigate(
                TodoListFragmentDirections.actionTodoListFragmentToCreateTodoFragment(

                ).setActiveCategoryId(todoListViewModel.activeCategory.value!!.id)
            )
            todoListViewModel.isNavigating(true)
        }

        with(todoListViewModel) {

            categoriesTransform.observe(viewLifecycleOwner) {}

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

            isLongPressed.observe(viewLifecycleOwner) { isEnabled ->
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

            selectionCount.observe(viewLifecycleOwner) { count ->
                actionMode?.let {
                    it.title = count.toString()
                    with(it.menu) {
                        findItem(R.id.select_all)?.isVisible = count != itemsState().size
                        findItem(R.id.edit)?.isVisible = count == 1
                        findItem(R.id.done_all)?.isVisible = count > 1
                    }
                }
            }

            readySummary.observe(viewLifecycleOwner) { }
        }

        val savedStateHandle = findNavController().currentBackStackEntry?.savedStateHandle
        savedStateHandle?.getLiveData<Int>(DEF_KEY)?.observe(viewLifecycleOwner) { value ->
            if (value != null) {
                todoListViewModel.emitAsActive(
                    value, savedStateHandle[LIST_STATE_KEY] ?: false
                )
                val isListAvailable: Boolean = savedStateHandle[LIST_STATE_KEY] ?: false
                if (isListAvailable) savedStateHandle.remove<Boolean>(LIST_STATE_KEY)
                savedStateHandle.remove<Int>(DEF_KEY)

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
            R.id.categoriesFragment -> {
                findNavController().navigate(
                    TodoListFragmentDirections.actionTodoListFragmentToCategoriesFragment2(ADDRESS)
                )
                todoListViewModel.isNavigating(true)
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

    override fun message(): String {
        return "Delete ${todoListViewModel.selectionCount.value!!} todos " +
                "from ${todoListViewModel.activeCategory.value!!.name} ?"
    }

    override fun positiveAction() {
        todoListViewModel.deleteSelected()
        todoListViewModel.interact()
        todoListViewModel.updateDiscarded()
    }
}