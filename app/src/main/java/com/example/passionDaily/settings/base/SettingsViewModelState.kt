package com.example.passionDaily.settings.base

import com.google.firebase.auth.FirebaseUser
import java.time.LocalTime
import kotlinx.coroutines.flow.StateFlow

interface SettingsViewModelState {
    val notificationEnabled: StateFlow<Boolean>
    val notificationTime: StateFlow<LocalTime?>
    val showWithdrawalDialog: StateFlow<Boolean>
    val currentUser: StateFlow<FirebaseUser?>
    val isLoading: StateFlow<Boolean>
}