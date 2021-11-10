package com.example.doit.database

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@ProvidedTypeConverter
class DateConverters {

    @TypeConverter
    fun LocalDateTime?.toString(): String? {
        return this?.let {
            var dateString: String?
            it.apply {
                val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
                dateString = format(formatter)
            }
            dateString
        }
    }

    @TypeConverter
    fun String?.toLocalDateTime(): LocalDateTime? {
        return this?.let {
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            LocalDateTime.parse(this, formatter)
        }
    }
}