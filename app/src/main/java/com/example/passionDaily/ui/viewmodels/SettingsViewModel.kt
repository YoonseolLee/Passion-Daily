package com.example.passionDaily.ui.viewmodels

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.R
import com.example.passionDaily.login.manager.AuthenticationManager
import com.example.passionDaily.manager.SettingsManager
import com.example.passionDaily.manager.alarm.DailyQuoteAlarmScheduler
import com.example.passionDaily.resources.StringProvider
import com.example.passionDaily.login.stateholder.AuthStateHolder
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestoreException
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
) : ViewModel() {

    companion object {
        private const val TAG = "SettingsViewModel"
    }

    private val _notificationEnabled = MutableStateFlow(false)
    val notificationEnabled: StateFlow<Boolean> = _notificationEnabled.asStateFlow()

    private val _notificationTime = MutableStateFlow<LocalTime?>(null)
    val notificationTime: StateFlow<LocalTime?> = _notificationTime.asStateFlow()

    private val _navigateToQuote = MutableStateFlow(false)
    val navigateToQuote: StateFlow<Boolean> = _navigateToQuote.asStateFlow()

    private val _navigateToLogin = MutableStateFlow(false)
    val navigateToLogin: StateFlow<Boolean> = _navigateToLogin.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

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
                safeSettingsOperation {
                    settingsManager.loadUserSettings(userId) { enabled, timeStr ->
                        _notificationEnabled.emit(enabled)
                        parseAndSetNotificationTime(timeStr)
                    }
                }
            }
        }
    }

    private fun getCurrentUser() = Firebase.auth.currentUser

    private suspend fun parseAndSetNotificationTime(timeStr: String?) {
        timeStr?.let {
            safeSettingsOperation {
                val time = LocalTime.parse(timeStr)
                _notificationTime.emit(time)
            }
        }
    }

    fun updateNotificationSettings(enabled: Boolean) {
        viewModelScope.launch {
            getCurrentUser()?.uid?.let { userId ->
                Log.d(TAG, "Attempting to update notification settings: enabled=$enabled for user=$userId")

                safeSettingsOperation {
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
                }
            } ?: Log.e(TAG, "Failed to update notification settings: User not logged in")
        }
    }

    fun updateNotificationTime(newTime: LocalTime) {
        viewModelScope.launch {
            getCurrentUser()?.uid?.let { userId ->
                Log.d(TAG, "Attempting to update notification time to ${newTime.hour}:${newTime.minute} for user=$userId")

                safeSettingsOperation {
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
                }
            } ?: Log.e(TAG, "Failed to update notification time: User not logged in")
        }
    }

    fun logIn() {
        viewModelScope.launch {
            _isLoading.emit(true)
            try {
                if (getCurrentUser() != null) {
                    _toastMessage.emit(stringProvider.getString(R.string.error_already_logged_in))
                    return@launch
                }
                _navigateToLogin.emit(true)
            } finally {
                _isLoading.emit(false)
            }
        }
    }

    fun logOut() {
        viewModelScope.launch {
            safeSettingsOperation {
                if (getCurrentUser() == null) {
                    _toastMessage.emit(stringProvider.getString(R.string.error_already_logged_out))
                    return@safeSettingsOperation
                }

                _isLoading.emit(true)
                try {
                    authManager.clearCredentials()
                    authStateHolder.setUnAuthenticated()
                    alarmScheduler.cancelExistingAlarm()

                    _toastMessage.emit(stringProvider.getString(R.string.success_logout))
                    _navigateToQuote.emit(true)
                } finally {
                    _isLoading.emit(false)
                }
            }
        }
    }

    fun withdrawUser() {
        viewModelScope.launch {
            safeSettingsOperation {
                _isLoading.emit(true)
                try {
                    val user = getCurrentUser() ?: run {
                        _toastMessage.emit(stringProvider.getString(R.string.error_login_required))
                        return@safeSettingsOperation
                    }

                    settingsManager.deleteUserData(user.uid)

                    // 계정 삭제 시도
                    Firebase.auth.currentUser?.let { currentUser ->
                        try {
                            currentUser.delete().await()
                            _toastMessage.emit(stringProvider.getString(R.string.success_withdrawal))
                            _navigateToQuote.emit(true)
                        } catch (e: FirebaseAuthRecentLoginRequiredException) {
                            _toastMessage.emit("보안을 위해 다시 로그인해주세요")
                            Firebase.auth.signOut()
                            _navigateToLogin.emit(true)
                        }
                    }
                } finally {
                    _isLoading.emit(false)
                }
            }
        }
    }

    fun createEmailIntent(): Intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:thisyoon97@gmail.com")
    }

    fun clearToastMessage() {
        viewModelScope.launch {
            _toastMessage.emit(null)
        }
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

    private suspend fun safeSettingsOperation(block: suspend () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            _isLoading.emit(false)
            Log.e(TAG, "Error in settings operation", e)
            _toastMessage.emit(stringProvider.getString(R.string.error_general))
        }
    }

    private fun mapExceptionToErrorMessage(e: Exception): String {
        return when (e) {
            is FirebaseFirestoreException -> when (e.code) {
                FirebaseFirestoreException.Code.UNAVAILABLE ->
                    stringProvider.getString(R.string.error_network)

                FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                    stringProvider.getString(R.string.error_permission_denied)

                else ->
                    stringProvider.getString(R.string.error_firebase_firestore)
            }

            is FirebaseAuthException ->
                stringProvider.getString(R.string.error_firebase_auth)

            is FirebaseAuthRecentLoginRequiredException ->
                stringProvider.getString(R.string.error_firebase_auth)


            else ->
                stringProvider.getString(R.string.error_general, e.message.orEmpty())
        }
    }
}