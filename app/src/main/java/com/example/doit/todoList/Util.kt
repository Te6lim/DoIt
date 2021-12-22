package com.example.doit.todoList

import com.example.doit.database.Category
import com.example.doit.todoList.categories.CategoryInfo
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
        isDefault = this.isDefault
    )
}

fun LocalDateTime.toMilliSeconds(): Long {
    return atZone(ZoneOffset.UTC).toInstant().toEpochMilli()
}