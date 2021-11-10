package com.example.doit.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Entity(tableName = "todo_table")
data class Todo(
    @PrimaryKey(autoGenerate = true)
    var todoId: Long = 0L,

    @ColumnInfo(name = "todo_text")
    var todoString: String = "",

    @ColumnInfo(name = "date_set")
    var dateSet: LocalDateTime = LocalDateTime.now(),

    @ColumnInfo(name = "date_todo")
    var dateTodo: LocalDateTime,

    @ColumnInfo(name = "date_finished")
    var dateFinished: LocalDateTime? = null,

    @ColumnInfo(name = "is_completed")
    var isCompleted: Boolean = false,

    @ColumnInfo(name = "has_deadline")
    val hasDeadline: Boolean = false,

    @ColumnInfo(name = "deadline_date")
    var deadlineDate: LocalDateTime? = null,

    @ColumnInfo(name = "category")
    var category: String
)