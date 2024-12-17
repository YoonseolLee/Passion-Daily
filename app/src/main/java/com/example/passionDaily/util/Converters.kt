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

//    @TypeConverter
//    fun fromLongToString(timestamp: Long): String {
//        return Instant.ofEpochMilli(timestamp)
//            .atZone(ZoneId.systemDefault())
//            .format(formatter)
//    }
//
//    @TypeConverter
//    fun fromGender(gender: Gender): String = gender.name
//
//    @TypeConverter
//    fun toGender(value: String): Gender = Gender.valueOf(value)
//
//    @TypeConverter
//    fun fromAgeGroup(ageGroup: AgeGroup): String = ageGroup.name
//
//    @TypeConverter
//    fun toAgeGroup(value: String): AgeGroup = AgeGroup.valueOf(value)
}