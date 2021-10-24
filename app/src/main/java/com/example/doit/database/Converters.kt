package com.example.doit.database

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@ProvidedTypeConverter
class DateConverters {

    @TypeConverter
    fun LocalDate?.toString(): String? {
        return this?.let {
            var dateString: String?
            it.apply {
                val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)
                dateString = format(formatter)
            }
            dateString
        }
    }

    @TypeConverter
    fun String?.toLocalDate(): LocalDate? {
        return this?.let {
            val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)
            LocalDate.parse(this, formatter)
        }
    }
}

@ProvidedTypeConverter
class TimeConverters {
    @TypeConverter
    fun LocalTime?.toString(): String? {
        return this?.let {
            var timeString: String?
            it.apply {
                val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)
                timeString = format(formatter)
            }
            timeString
        }
    }

    @TypeConverter
    fun String?.toLocalTime(): LocalTime? {
        return this?.let {
            val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)
            LocalTime.parse(this, formatter)
        }
    }
}