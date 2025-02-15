package com.example.passionDaily.settings.manager

import com.example.passionDaily.notification.usecase.ScheduleDailyQuoteAlarmUseCase
import com.example.passionDaily.settings.domain.usecase.ParseTimeUseCase
import com.example.passionDaily.settings.domain.usecase.SaveNotificationUseCase
import com.example.passionDaily.settings.domain.usecase.UpdateNotificationUseCase
import java.time.LocalTime
import javax.inject.Inject

class NotificationManagerImpl @Inject constructor(
    private val updateNotificationUseCase: UpdateNotificationUseCase,
    private val scheduleAlarmUseCase: ScheduleDailyQuoteAlarmUseCase,
    private val parseTimeUseCase: ParseTimeUseCase,
    private val saveNotificationUseCase: SaveNotificationUseCase
) : NotificationManager {
    override suspend fun updateNotificationSettingsToFirestore(userId: String, enabled: Boolean) {
        updateNotificationUseCase.updateNotificationSettingsToFirestore(userId, enabled)
    }

    override suspend fun updateNotificationSettingsToRoom(userId: String, enabled: Boolean) {
        updateNotificationUseCase.updateNotificationSettingsToRoom(userId, enabled)
    }

    override fun scheduleNotification(hour: Int, minute: Int) {
        scheduleAlarmUseCase.scheduleNotification(hour, minute)
    }

    override fun cancelExistingAlarm() {
        scheduleAlarmUseCase.cancelExistingAlarm()
    }

    override fun parseTime(timeStr: String): LocalTime {
        return parseTimeUseCase.parseTime(timeStr)
    }

    override suspend fun updateNotificationTimeToFirestore(userId: String, time: LocalTime) {
        saveNotificationUseCase.updateNotificationTimeToFirestore(userId, time)
    }

    override suspend fun updateNotificationTimeToRoom(userId: String, time: LocalTime) {
        saveNotificationUseCase.updateNotificationTimeToRoom(userId, time)
    }
}
