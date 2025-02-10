package com.example.passionDaily.settings.state

import com.example.passionDaily.settings.base.SettingsViewModelState
import java.time.LocalTime
import kotlinx.coroutines.flow.StateFlow

interface NotificationSettingsState : SettingsViewModelState {
    override val notificationEnabled: StateFlow<Boolean>
    override val notificationTime: StateFlow<LocalTime>
}