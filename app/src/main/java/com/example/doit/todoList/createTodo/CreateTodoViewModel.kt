package com.example.doit.todoList.createTodo

import androidx.lifecycle.*
import com.example.doit.database.Category
import com.example.doit.database.CategoryDao
import com.example.doit.database.Todo
import com.example.doit.database.TodoDbDao
import kotlinx.coroutines.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class CreateTodoViewModel(
    private val todoDb: TodoDbDao, private val catDb: CategoryDao,
    private val editTodoId: Long
) : ViewModel() {

    companion object {
        const val DEFAULT_CATEGORY = "Work"
    }

    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    val categories = catDb.getAll()

    private val _todoModel = MutableLiveData<TodoModel>()
    val todoModel: LiveData<TodoModel>
        get() = _todoModel

    var model = TodoModel()
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

    fun add(todoModel: TodoModel) {
        uiScope.launch {
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
            withContext(Dispatchers.IO) {
                if (editTodo.value != null) todoDb.update(todo.apply { todoId = editTodoId })
                else todoDb.insert(todo)
            }
        }
    }

    fun createTodoInfo() {
        _todoModel.value = model
    }

    fun clearTodoInfo() {
        model = TodoModel()
        _todoModel.value = model
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
                model.setDescription(todoString)
                model.setDateTodo(dateTodo.year, dateTodo.monthValue, dateTodo.dayOfMonth)
                model.setTimeTodo(dateTodo.toLocalTime().hour, dateTodo.toLocalTime().minute)
                if (todoEdit.hasDeadline) {
                    model.setHasDeadlineEnabled(true)
                    model.setDeadlineDate(
                        deadlineDate!!.year,
                        deadlineDate!!.monthValue,
                        deadlineDate!!.dayOfMonth
                    )
                    model.setDeadlineTime(
                        deadlineDate!!.hour,
                        deadlineDate!!.minute
                    )
                } else {
                    model.setHasDeadlineEnabled(false)
                }
            }
        }
    }

    override fun onCleared() {
        uiScope.cancel()
    }
}

class CreateTodoViewModelFactory(
    private val todoDb: TodoDbDao, private val catDb: CategoryDao, private val editTodo: Long
) :
    ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateTodoViewModel::class.java)) {
            return CreateTodoViewModel(todoDb, catDb, editTodo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}