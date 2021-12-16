package com.example.doit.todoList.createTodo

import androidx.lifecycle.*
import com.example.doit.database.*
import kotlinx.coroutines.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class CreateTodoViewModel(
    private val todoDb: TodoDbDao, private val catDb: CategoryDao,
    private val summaryDb: SummaryDao,
    private val editTodoId: Long
) : ViewModel() {

    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    val categories = catDb.getAll()

    private val summary = summaryDb.getSummary()
    val readySummary = fetchReadySummary()

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
        uiScope.launch {
            if (editTodo.value != null) {
                todoDb.update(editTodo.value!!.apply {
                    todoString = todoModel.description.value!!
                    catId = category.value!!.id
                    dateTodo = LocalDateTime.of(
                        todoModel.dateTodoLive.value!!,
                        todoModel.timeTodoLive.value!!
                    )
                    hasDeadline = todoModel.hasDeadline.value!!
                    if (hasDeadline) {
                        deadlineDate =
                            LocalDateTime.of(
                                todoModel.deadlineDateLive.value!!.toLocalDate(),
                                todoModel.deadlineDateLive.value!!.toLocalTime()
                            )
                    }
                })
                clearTodoInfo()
            } else {
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
                    todoDb.insert(todo)
                    catDb.update(categories.value!!.find { it.id == todo.catId }!!.apply {
                        this.totalCreated += 1
                    })
                }
            }
        }

        _todoCreated.value = true
    }

    private fun clearTodoInfo() {
        todoModel = TodoModel()
        _todoCreated.postValue(false)
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

    private fun fetchReadySummary(): LiveData<Summary> {
        val result = MediatorLiveData<Summary>()
        val action = Observer<Summary?> {
            if (it == null) {
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        summaryDb.insert(Summary())
                    }
                }
            } else {
                result.value = it
            }
        }
        result.addSource(summary, action)
        return result
    }

    fun updateCreatedCount() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                summaryDb.insert(summary.value!!.apply {
                    todosCreated += 1
                })

            }
        }
    }

    override fun onCleared() {
        uiScope.cancel()
    }
}

class CreateTodoViewModelFactory(
    private val todoDb: TodoDbDao, private val catDb: CategoryDao,
    private val summaryDb: SummaryDao,
    private val editTodo: Long
) :
    ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateTodoViewModel::class.java)) {
            return CreateTodoViewModel(todoDb, catDb, summaryDb, editTodo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}