package com.example.passionDaily.settings.manager

import com.example.passionDaily.manager.alarm.ScheduleDailyQuoteAlarmUseCase
import com.example.passionDaily.settings.usecase.UpdateNotificationUseCase
import javax.inject.Inject

class NotificationManager @Inject constructor(
    private val updateNotificationUseCase: UpdateNotificationUseCase,
    private val scheduleAlarmUseCase: ScheduleDailyQuoteAlarmUseCase
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
}