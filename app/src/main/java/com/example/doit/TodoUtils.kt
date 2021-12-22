package com.example.doit

import java.time.LocalDateTime
import java.time.ZoneId

fun LocalDateTime.toMilliSeconds(): Long {
    return atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}