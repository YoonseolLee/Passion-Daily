package com.example.passionDaily.util

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object Converters {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    @TypeConverter
    fun fromStringToLong(dateString: String): Long {
        return LocalDateTime.parse(dateString, formatter)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }
}