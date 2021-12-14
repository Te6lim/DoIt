package com.example.doit.todoList.createTodo

import androidx.lifecycle.*
import com.example.doit.database.Category
import com.example.doit.database.CategoryDao
import com.example.doit.database.Todo
import com.example.doit.database.TodoDbDao
import com.example.doit.summary.SummaryViewModel
import com.example.doit.summary.SummaryViewModelFactory
import kotlinx.coroutines.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class CreateTodoViewModel(
    private val store: ViewModelStore,
    private val todoDb: TodoDbDao, private val catDb: CategoryDao,
    private val editTodoId: Long
) : ViewModel() {

    companion object {
        const val DEFAULT_CATEGORY = "Work"
    }

    private val summaryViewModel = ViewModelProvider(
        store, SummaryViewModelFactory(catDb, todoDb)
    )[SummaryViewModel::class.java]

    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    val categories = catDb.getAll()

    private val _todoCreated = MutableLiveData<Boolean>()
    val todoCreated: LiveData<Boolean>
        get() = _todoCreated

    var todoModel = TodoModel()
        private set

    private val _categoryEditTextIsOpen = MutableLiveData<Boolean>()
    val categoryEditTextIsOpen: LiveData<Boolean>
        get() = _categoryEditTextIsOpen

    private val _category = MutableLiveData<Category>()
    val category: LiveData<Category>
        get() = _category

    private val _editTodo = MutableLiveData<Todo?>()
    val editTodo = Transformations.map(_editTodo) {
        it?.let {
            initializeFields(it)
            emitCategory(it.catId)
            it
        }
    }

    init {
        initializeEditTodo()
    }

    private fun initializeEditTodo() {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                _editTodo.postValue(todoDb.get(editTodoId))
            }
        }
    }

    fun createTodoInfo() {
        val todo = Todo(
            todoString = todoModel.description.value!!,
            catId = category.value!!.id,
            dateTodo = LocalDateTime.of(
                todoModel.dateTodoLive.value ?: LocalDate.now(),
                todoModel.timeTodoLive.value ?: LocalTime.now()
            ),
            hasDeadline = todoModel.hasDeadline.value!!,
        ).apply {
            if (hasDeadline) {
                deadlineDate =
                    LocalDateTime.of(
                        todoModel.deadlineDateLive.value!!.toLocalDate(),
                        todoModel.deadlineDateLive.value!!.toLocalTime()
                    )
            }
        }
        uiScope.launch {
            withContext(Dispatchers.IO) {
                if (editTodo.value != null) todoDb.update(todo.apply { todoId = editTodoId })
                else {
                    summaryViewModel.setIsTodoCreated(true)
                    todoDb.insert(todo)
                    summaryViewModel.setIsTodoCreated(false)
                }
            }
        }

        _todoCreated.value = true
    }

    fun clearTodoInfo() {
        todoModel = TodoModel()
        _todoCreated.value = false
    }

    fun addNewCategory(newCategory: String, default: Boolean = false) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                catDb.insert(Category(name = newCategory, isDefault = default))
            }
        }
    }

    fun emitCategory(id: Int) {
        uiScope.launch {
            _category.value = getCat(id)
        }
    }

    private suspend fun getCat(id: Int): Category? {
        return withContext(Dispatchers.IO) {
            catDb.get(id)
        }
    }

    fun makeCategoryEditTextVisible() {
        _categoryEditTextIsOpen.value = true
    }

    fun makeCategoryEditTextNotVisible() {
        _categoryEditTextIsOpen.value = false
    }

    private fun initializeFields(editTodo: Todo) {
        uiScope.launch {
            val todoEdit = editTodo
            with(editTodo) {
                todoModel.setDescription(todoString)
                todoModel.setDateTodo(dateTodo.year, dateTodo.monthValue, dateTodo.dayOfMonth)
                todoModel.setTimeTodo(dateTodo.toLocalTime().hour, dateTodo.toLocalTime().minute)
                if (todoEdit.hasDeadline) {
                    todoModel.setHasDeadlineEnabled(true)
                    todoModel.setDeadlineDate(
                        deadlineDate!!.year,
                        deadlineDate!!.monthValue,
                        deadlineDate!!.dayOfMonth
                    )
                    todoModel.setDeadlineTime(
                        deadlineDate!!.hour,
                        deadlineDate!!.minute
                    )
                } else {
                    todoModel.setHasDeadlineEnabled(false)
                }
            }
        }
    }

    fun isEditTodoActive() = editTodoId != -1L

    override fun onCleared() {
        uiScope.cancel()
    }
}

class CreateTodoViewModelFactory(
    private val store: ViewModelStore,
    private val todoDb: TodoDbDao, private val catDb: CategoryDao, private val editTodo: Long
) :
    ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateTodoViewModel::class.java)) {
            return CreateTodoViewModel(store, todoDb, catDb, editTodo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}