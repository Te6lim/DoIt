package com.example.doit.todoList

import com.example.doit.database.Category
import com.example.doit.todoList.categories.CategoryInfo
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


fun LocalDate.formatToString(formatter: DateTimeFormatter): String {
    return format(formatter)
}

fun LocalTime.formatToString(): String {
    val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)
    return format(formatter)
}

fun CategoryInfo.toCategory(): Category {
    return Category(
        id = this.id,
        name = this.name,
        isDefault = this.isDefault
    )
}