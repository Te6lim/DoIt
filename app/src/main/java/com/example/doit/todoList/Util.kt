package com.example.doit.todoList

import com.example.doit.database.Category
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

fun List<Category>?.toListOfString(f: (Category) -> String): List<String> {
    val list = mutableListOf<String>()
    this?.let {
        for (item in this) {
            list.add(f(item))
        }
    }
    return list
}