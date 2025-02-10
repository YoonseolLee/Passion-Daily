package com.example.passionDaily.settings.manager

import java.time.LocalTime

interface NotificationManager {
    suspend fun updateNotificationSettings(userId: String, enabled: Boolean)
    fun scheduleNotification(hour: Int, minute: Int)
    fun cancelExistingAlarm()
    fun parseTime(timeStr: String): LocalTime
    suspend fun updateNotificationTime(userId: String, time: LocalTime)
}
