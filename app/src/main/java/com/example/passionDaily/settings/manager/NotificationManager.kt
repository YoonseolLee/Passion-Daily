package com.example.passionDaily.settings.manager

import java.time.LocalTime

interface NotificationManager {
    suspend fun updateNotificationSettingsToFirestore(userId: String, enabled: Boolean)
    suspend fun updateNotificationSettingsToRoom(userId: String, enabled: Boolean)
    fun scheduleNotification(hour: Int, minute: Int)
    fun cancelExistingAlarm()
    fun parseTime(timeStr: String): LocalTime
    suspend fun updateNotificationTimeToFirestore(userId: String, time: LocalTime)
    suspend fun updateNotificationTimeToRoom(userId: String, time: LocalTime)
}
