package com.example.doit.todoList.createTodo

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.core.app.AlarmManagerCompat
import androidx.lifecycle.*
import com.example.doit.broadcasts.AlarmReceiver
import com.example.doit.database.*
import com.example.doit.todoList.toMilliSeconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class CreateTodoViewModel(
    private val app: Application,
    private val todoDb: TodoDbDao, private val catDb: CategoryDao,
    private val editTodoId: Long,
    catId: Int,
    private val summaryDb: SummaryDao
) : AndroidViewModel(app) {

    companion object {
        private const val REQUEST_CODE = 0
        const val DEADLINE_NOTIFICATION = "deadline notification"
        const val TODO_STRING = "todo string"
    }

    private val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager

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

    var activeCategoryId: Int = catId
    private set

    private val _categoryLive = MutableLiveData<Category>()
    val categoryLive: LiveData<Category>
        get() = _categoryLive

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
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _editTodo.postValue(todoDb.get(editTodoId))
            }
        }
    }

    fun createTodoInfo() {
        viewModelScope.launch {
            editTodo.value?.let {
                withContext(Dispatchers.IO) {
                    todoDb.update(editTodo.value!!.apply {
                        todoString = todoModel.description.value!!
                        catId = categoryLive.value!!.id
                        dateTodo = LocalDateTime.of(
                            todoModel.dateTodoLive.value!!, todoModel.timeTodoLive.value!!
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

                    if (editTodo.value!!.hasDeadline) setAlarm(editTodo.value!!)

                    clearTodoInfo()
                }
            } ?: run {
                val todo = Todo(
                    todoString = todoModel.description.value!!,
                    catId = categoryLive.value!!.id,
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
                    summaryDb.insert(summary.value!!.apply {
                        todosCreated += 1
                    })
                    catDb.update(categories.value!!.find { it.id == todo.catId }!!.apply {
                        this.totalCreated += 1
                    })
                }

                if (todo.hasDeadline) setAlarm(todo)
            }
        }

        _todoCreated.value = true
    }

    private fun setAlarm(todo: Todo) {
        val notifyIntent = Intent(
            app, AlarmReceiver::class.java
        ).apply { putExtra(TODO_STRING, todo.todoString) }

        @SuppressLint("UnspecifiedImmutableFlag")
        val pendingIntent = PendingIntent.getBroadcast(
            app, REQUEST_CODE, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        val duration = todo.deadlineDate!!
            .toMilliSeconds() - LocalDateTime.now().toMilliSeconds()
        AlarmManagerCompat.setExactAndAllowWhileIdle(
            alarmManager, AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + duration, pendingIntent
        )
    }

    private fun clearTodoInfo() {
        todoModel = TodoModel()
        _todoCreated.postValue(false)
    }

    fun addNewCategory(newCategory: String, default: Boolean = false) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val new = Category(name = newCategory, isDefault = default)
                catDb.insert(new)
            }
        }
    }

    fun emitCategory(id: Int) {
        viewModelScope.launch {
            _categoryLive.value = getCat(id).apply { activeCategoryId = id }
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
        viewModelScope.launch {
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

}

class CreateTodoViewModelFactory(
    private val app: Application,
    private val todoDb: TodoDbDao, private val catDb: CategoryDao,
    private val editTodo: Long,
    private val catId: Int,
    private val summaryDb: SummaryDao,
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateTodoViewModel::class.java)) {
            return CreateTodoViewModel(app, todoDb, catDb, editTodo, catId, summaryDb) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}