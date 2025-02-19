package com.example.passionDaily.settings.stateholder

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalTime

class SettingsStateHolderImpl : SettingsStateHolder {
    private val _notificationEnabled = MutableStateFlow(false)
    override val notificationEnabled: StateFlow<Boolean> = _notificationEnabled.asStateFlow()

    private val _notificationTime = MutableStateFlow<LocalTime?>(null)
    override val notificationTime: StateFlow<LocalTime?> = _notificationTime.asStateFlow()

    private val _showWithdrawalDialog = MutableStateFlow(false)
    override val showWithdrawalDialog: StateFlow<Boolean> = _showWithdrawalDialog.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    override val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    override fun updateNotificationEnabled(enabled: Boolean) {
        _notificationEnabled.value = enabled
    }

    override fun updateNotificationTime(time: LocalTime?) {
        _notificationTime.value = time ?: LocalTime.of(8, 0)
    }

    override fun updateShowWithdrawalDialog(show: Boolean) {
        _showWithdrawalDialog.value = show
    }

    override fun updateCurrentUser(user: FirebaseUser?) {
        _currentUser.value = user
    }

    override fun updateIsLoading(loading: Boolean) {
        _isLoading.value = loading
    }
}