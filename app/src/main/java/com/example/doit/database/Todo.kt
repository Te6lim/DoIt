package com.example.doit.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

@Entity(tableName = "todo_table")
data class Todo(
    @PrimaryKey(autoGenerate = true)
    var todoId: Long = 0L,

    @ColumnInfo(name = "todo_text")
    var todoString: String = "",

    @ColumnInfo(name = "date_set")
    var dateSet: LocalDate = LocalDate.now(),

    @ColumnInfo(name = "time_set")
    var timeSet: LocalTime = LocalTime.now(),

    @ColumnInfo(name = "date_todo")
    var dateTodo: LocalDate,

    @ColumnInfo(name = "time_todo")
    var timeTodo: LocalTime,

    @ColumnInfo(name = "date_finished")
    var dateFinished: LocalDate? = null,

    @ColumnInfo(name = "time_finished")
    var timeFinished: LocalTime? = null,

    @ColumnInfo(name = "is_completed")
    var isCompleted: Boolean = false,

    @ColumnInfo(name = "has_deadline")
    val hasDeadline: Boolean = false,

    @ColumnInfo(name = "deadline_date")
    var deadlineDate: LocalDate? = null,

    @ColumnInfo(name = "deadline_time")
    var deadlineTime: LocalTime? = null,

    @ColumnInfo(name = "category")
    var category: String
)