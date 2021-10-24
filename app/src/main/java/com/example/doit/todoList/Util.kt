package com.example.doit.todoList

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


fun LocalDate.formatToString(): String {
    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)
    return format(formatter)
}

fun LocalTime.formatToString(): String {
    val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)
    return format(formatter)
}