package com.example.passionDaily.util

import android.util.Log
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object TimeUtil {
    fun getCurrentTimestamp(): String {
        return LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    }

    fun parseTimestamp(timestamp: String): Long {
        return try {
            val date = parseToDate(timestamp)
            Log.d("parseTimestamp", "Parsed timestamp to milliseconds: ${date.time}")
            date.time
        } catch (e: Exception) {
            Log.e("TimestampParsing", "Failed to parse timestamp: $timestamp", e)
            System.currentTimeMillis()
        }
    }

    private fun parseToDate(timestamp: String): Date {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return dateFormat.parse(timestamp)
            ?: throw IllegalArgumentException("Invalid date format: $timestamp")
    }
}