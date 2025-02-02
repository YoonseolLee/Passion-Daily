package com.example.passionDaily.settings.presentation.viewmodel

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.R
import com.example.passionDaily.constants.ViewModelConstants.Settings.TAG
import com.example.passionDaily.login.manager.AuthenticationManager
import com.example.passionDaily.settings.manager.SettingsManager
import com.example.passionDaily.manager.alarm.DailyQuoteAlarmScheduler
import com.example.passionDaily.resources.StringProvider
import com.example.passionDaily.login.stateholder.AuthStateHolder
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsManager: SettingsManager,
    private val stringProvider: StringProvider,
    private val alarmScheduler: DailyQuoteAlarmScheduler,
    private val authManager: AuthenticationManager,
    private val authStateHolder: AuthStateHolder,
    private val toastManager: ToastManager
) : ViewModel() {

    private val _notificationEnabled = MutableStateFlow(false)
    val notificationEnabled: StateFlow<Boolean> = _notificationEnabled.asStateFlow()

    private val _notificationTime = MutableStateFlow<LocalTime?>(null)
    val notificationTime: StateFlow<LocalTime?> = _notificationTime.asStateFlow()

    private val _navigateToQuote = MutableStateFlow(false)
    val navigateToQuote: StateFlow<Boolean> = _navigateToQuote.asStateFlow()

    private val _navigateToLogin = MutableStateFlow(false)
    val navigateToLogin: StateFlow<Boolean> = _navigateToLogin.asStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()

    private val _showWithdrawalDialog = MutableStateFlow(false)
    val showWithdrawalDialog: StateFlow<Boolean> = _showWithdrawalDialog.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        _currentUser.value = getCurrentUser()

        viewModelScope.launch {
            getCurrentUser()?.uid?.let { userId ->
                try {
                    settingsManager.loadUserSettings(userId) { enabled, timeStr ->
                        _notificationEnabled.emit(enabled)
                        parseAndSetNotificationTime(timeStr)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading user settings", e)
                    toastManager.showGeneralErrorToast()
                }
            }
        }
    }

    private fun getCurrentUser() = Firebase.auth.currentUser

    private suspend fun parseAndSetNotificationTime(timeStr: String?) {
        timeStr?.let {
            try {
                val time = LocalTime.parse(timeStr)
                _notificationTime.emit(time)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing notification time", e)
                toastManager.showGeneralErrorToast()
            }
        }
    }

    fun updateNotificationSettings(enabled: Boolean) {
        viewModelScope.launch {
            getCurrentUser()?.uid?.let { userId ->
                Log.d(TAG, "Attempting to update notification settings: enabled=$enabled for user=$userId")

                try {
                    // Firestore와 Room에 설정 업데이트
                    settingsManager.updateNotificationSettings(userId, enabled)
                    Log.d(TAG, "Successfully updated notification settings in databases")

                    _notificationEnabled.emit(enabled)
                    Log.d(TAG, "Updated local notification enabled state")

                    // 알람 관리
                    if (enabled) {
                        notificationTime.value?.let { time ->
                            Log.d(TAG, "Notification enabled, scheduling alarm for ${time.hour}:${time.minute}")
                            alarmScheduler.scheduleNotification(time.hour, time.minute)
                        } ?: Log.w(TAG, "Notification enabled but no time set")
                    } else {
                        Log.d(TAG, "Notification disabled, canceling existing alarm")
                        alarmScheduler.cancelExistingAlarm()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating notification settings", e)
                    toastManager.showGeneralErrorToast()
                }
            } ?: Log.e(TAG, "Failed to update notification settings: User not logged in")
        }
    }

    fun updateNotificationTime(newTime: LocalTime) {
        viewModelScope.launch {
            getCurrentUser()?.uid?.let { userId ->
                Log.d(TAG, "Attempting to update notification time to ${newTime.hour}:${newTime.minute} for user=$userId")

                try {
                    // Firestore와 Room에 시간 업데이트
                    settingsManager.updateNotificationTime(userId, newTime)
                    Log.d(TAG, "Successfully updated notification time in databases")

                    _notificationTime.emit(newTime)
                    Log.d(TAG, "Updated local notification time state")

                    // 알림이 활성화된 상태에서만 알람 재설정
                    if (notificationEnabled.value) {
                        Log.d(TAG, "Notifications are enabled, rescheduling alarm for new time ${newTime.hour}:${newTime.minute}")
                        alarmScheduler.scheduleNotification(newTime.hour, newTime.minute)
                    } else {
                        Log.d(TAG, "Notifications are disabled, skipping alarm scheduling")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating notification time", e)
                    toastManager.showGeneralErrorToast()
                }
            } ?: Log.e(TAG, "Failed to update notification time: User not logged in")
        }
    }

    fun logIn() {
        viewModelScope.launch {
            _isLoading.emit(true)
            try {
                if (getCurrentUser() != null) {
                    toastManager.showAlreadyLoggedInErrorToast()
                    return@launch
                }
                _navigateToLogin.emit(true)
            } catch (e: Exception) {
                Log.e(TAG, "Error during login", e)
                toastManager.showGeneralErrorToast()
            } finally {
                _isLoading.emit(false)
            }
        }
    }

    fun logOut() {
        viewModelScope.launch {
            try {
                if (getCurrentUser() == null) {
                    toastManager.showAlreadyLoggedOutErrorToast()
                    return@launch
                }

                _isLoading.emit(true)
                authManager.clearCredentials()
                authStateHolder.setUnAuthenticated()
                alarmScheduler.cancelExistingAlarm()

                toastManager.showLogoutSuccessToast()
                _navigateToQuote.emit(true)
            } catch (e: Exception) {
                Log.e(TAG, "Error during logout", e)
                toastManager.showGeneralErrorToast()
            } finally {
                _isLoading.emit(false)
            }
        }
    }

    fun withdrawUser() {
        viewModelScope.launch {
            _isLoading.emit(true)
            try {
                val user = getCurrentUser() ?: run {
                    toastManager.showLogInRequiredErrorToast()
                    return@launch
                }

                settingsManager.deleteUserData(user.uid)

                // 계정 삭제 시도
                Firebase.auth.currentUser?.let { currentUser ->
                    try {
                        currentUser.delete().await()
                        toastManager.showWithDrawlSuccessToast()
                        _navigateToQuote.emit(true)
                    } catch (e: FirebaseAuthRecentLoginRequiredException) {
                        toastManager.showReLoginForWithDrawlToast()
                        Firebase.auth.signOut()
                        _navigateToLogin.emit(true)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during withdrawal", e)
                toastManager.showGeneralErrorToast()
            } finally {
                _isLoading.emit(false)
            }
        }
    }

    fun createEmailIntent(): Intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:thisyoon97@gmail.com")
    }

    fun clearError() {
        viewModelScope.launch {
            _emailError.emit(null)
        }
    }

    fun onNavigatedToQuote() {
        viewModelScope.launch {
            _navigateToQuote.emit(false)
        }
    }

    fun onNavigatedToLogin() {
        viewModelScope.launch {
            _navigateToLogin.emit(false)
        }
    }

    fun setError(message: String) {
        viewModelScope.launch {
            _emailError.emit(message)
        }
    }

    fun updateShowWithdrawalDialog(show: Boolean) {
        viewModelScope.launch {
            _showWithdrawalDialog.emit(show)
        }
    }
}