package com.example.passionDaily.settings.stateholder

import kotlinx.coroutines.flow.StateFlow
import java.time.LocalTime
import com.google.firebase.auth.FirebaseUser

interface SettingsStateHolder {
    val notificationEnabled: StateFlow<Boolean>
    val notificationTime: StateFlow<LocalTime?>
    val showWithdrawalDialog: StateFlow<Boolean>
    val currentUser: StateFlow<FirebaseUser?>
    val isLoading: StateFlow<Boolean>

    fun updateNotificationEnabled(enabled: Boolean)
    fun updateNotificationTime(time: LocalTime?)
    fun updateShowWithdrawalDialog(show: Boolean)
    fun updateCurrentUser(user: FirebaseUser?)
    fun updateIsLoading(loading: Boolean)
}