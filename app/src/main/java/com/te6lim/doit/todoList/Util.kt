package com.te6lim.doit.todoList

import com.te6lim.doit.database.Category
import com.te6lim.doit.todoList.categories.CategoryInfo
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


fun LocalDate.formatToString(formatter: DateTimeFormatter): String {
    return format(formatter)
}

fun LocalTime.formatToString(): String {
    val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
    return format(formatter)
}

fun CategoryInfo.toCategory(): Category {
    return Category(
        id = this.id,
        name = this.name,
        isDefault = this.isDefault,
        totalCreated = this.totalCreated,
        totalFinished = this.totalFinished,
        totalSuccess = this.totalSuccess,
        totalFailure = this.totalFailure,
        lateTodos = this.lateTodos
    )
}

fun LocalDateTime.toMilliSeconds(): Long {
    return atZone(ZoneOffset.UTC).toInstant().toEpochMilli()
}