package com.example.passionDaily.settings.manager

import com.example.passionDaily.notification.usecase.ScheduleDailyQuoteAlarmUseCase
import com.example.passionDaily.settings.domain.usecase.ParseTimeUseCase
import com.example.passionDaily.settings.domain.usecase.SaveNotificationUseCase
import com.example.passionDaily.settings.domain.usecase.UpdateNotificationUseCase
import java.time.LocalTime
import javax.inject.Inject

class NotificationManager @Inject constructor(
    private val updateNotificationUseCase: UpdateNotificationUseCase,
    private val scheduleAlarmUseCase: ScheduleDailyQuoteAlarmUseCase,
    private val parseTimeUseCase: ParseTimeUseCase,
    private val saveNotificationUseCase: SaveNotificationUseCase
) {
    suspend fun updateNotificationSettings(userId: String, enabled: Boolean) {
        updateNotificationUseCase.updateNotificationSettings(userId, enabled)
    }

    fun scheduleNotification(hour: Int, minute: Int) {
        scheduleAlarmUseCase.scheduleNotification(hour, minute)
    }

    fun cancelExistingAlarm() {
        scheduleAlarmUseCase.cancelExistingAlarm()
    }

    fun parseTime(timeStr: String): LocalTime {
        return parseTimeUseCase.parseTime(timeStr)
    }

    suspend fun updateNotificationTime(userId: String, time: LocalTime) {
        saveNotificationUseCase.updateNotificationTime(userId, time)
    }
}