package com.example.doit.todoList.createTodo

import androidx.lifecycle.*
import com.example.doit.database.Category
import com.example.doit.database.CategoryDao
import com.example.doit.database.Todo
import com.example.doit.database.TodoDbDao
import kotlinx.coroutines.*
import java.time.LocalDateTime

class CreateTodoViewModel(
    private val todoDb: TodoDbDao, private val catDb: CategoryDao, private val editTodoId: Long
) : ViewModel() {

    companion object {
        const val DEFAULT_CATEGORY = "Work"
    }

    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    val categories = catDb.getAll()

    private val _todoInfo = MutableLiveData<TodoInfo>()
    val todoInfo: LiveData<TodoInfo>
        get() = _todoInfo

    var todo = TodoInfo()
        private set

    private val _categoryEditTextIsOpen = MutableLiveData<Boolean>()
    val categoryEditTextIsOpen: LiveData<Boolean>
        get() = _categoryEditTextIsOpen

    private val _category = MutableLiveData<Category>()
    val category: LiveData<Category>
        get() = _category

    private val _editTodo = MutableLiveData<Todo?>()
    val editTodo: LiveData<Todo?>
        get() = _editTodo

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

    fun add(todoInfo: TodoInfo) {
        uiScope.launch {
            val todo = Todo(
                todoString = todoInfo.description,
                catId = todoInfo.category,
                dateTodo = LocalDateTime.of(todoInfo.dateSet, todoInfo.timeSet),
                hasDeadline = todoInfo.deadlineEnabled.value!!,
            ).apply {
                if (hasDeadline) {
                    deadlineDate = LocalDateTime.of(todoInfo.deadlineDate, todoInfo.deadlineTime)
                }
            }
            withContext(Dispatchers.IO) {
                if (editTodo.value != null) todoDb.update(todo.apply { todoId = todoInfo.id })
                else todoDb.insert(todo)
            }
        }
    }

    fun createTodoInfo() {
        _todoInfo.value = todo
    }

    fun clearTodoInfo() {
        todo = TodoInfo()
        _todoInfo.value = todo
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

    fun initializeFields() {
        uiScope.launch {
            val todoEdit = editTodo.value!!
            with(editTodo.value!!) {
                todo.id = todoEdit.todoId
                todo.setDescription(todoString)
                todo.setDate(dateTodo.year, dateTodo.monthValue, dateTodo.dayOfMonth)
                todo.setTime(dateTodo.toLocalTime().hour, dateTodo.toLocalTime().minute)
                if (todoEdit.hasDeadline) {
                    todo.setIsDeadlineEnabled(true)
                    todo.setDeadlineDate(
                        deadlineDate!!.year,
                        deadlineDate!!.monthValue,
                        deadlineDate!!.dayOfMonth
                    )
                    todo.setDeadlineTime(
                        deadlineDate!!.hour,
                        deadlineDate!!.minute
                    )
                } else {
                    todo.setIsDeadlineEnabled(false)
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