package com.example.passionDaily.settings.action

import com.example.passionDaily.settings.base.SettingsViewModelActions
import java.time.LocalTime

interface NotificationSettingsActions : SettingsViewModelActions {
    override fun updateNotificationSettings(enabled: Boolean)
    override fun updateNotificationTime(newTime: LocalTime)
}