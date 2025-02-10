package com.example.passionDaily.settings.base

import android.content.Intent
import java.time.LocalTime

interface SettingsViewModelActions {
    fun loadUserSettings()
    fun updateNotificationSettings(enabled: Boolean)
    fun updateNotificationTime(newTime: LocalTime)
    fun logIn(onLogInSuccess: () -> Unit)
    fun logOut(onLogoutSuccess: () -> Unit)
    fun withdrawUser(onWithdrawSuccess: () -> Unit, onReLogInRequired: () -> Unit)
    fun createEmailIntent(): Intent?
    fun updateShowWithdrawalDialog(show: Boolean)
}