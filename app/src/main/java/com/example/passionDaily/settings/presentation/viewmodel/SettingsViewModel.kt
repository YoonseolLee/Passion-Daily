package com.example.passionDaily.settings.presentation.viewmodel

import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.constants.ViewModelConstants.Settings.TAG
import com.example.passionDaily.login.manager.AuthenticationManager
import com.example.passionDaily.settings.manager.UserSettingsManager
import com.example.passionDaily.manager.alarm.ScheduleDailyQuoteAlarmUseCase
import com.example.passionDaily.login.stateholder.AuthStateHolder
import com.example.passionDaily.settings.manager.EmailManager
import com.example.passionDaily.settings.manager.NotificationManager
import com.example.passionDaily.settings.stateholder.SettingsStateHolder
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.net.URISyntaxException
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userSettingsManager: UserSettingsManager,
    private val scheduleAlarmUseCase: ScheduleDailyQuoteAlarmUseCase,
    private val authManager: AuthenticationManager,
    private val notificationManager: NotificationManager,
    private val authStateHolder: AuthStateHolder,
    private val toastManager: ToastManager,
    private val emailManager: EmailManager,
    private val settingsStateHolder: SettingsStateHolder
) : ViewModel() {

    val notificationEnabled = settingsStateHolder.notificationEnabled
    val notificationTime = settingsStateHolder.notificationTime
    val showWithdrawalDialog = settingsStateHolder.showWithdrawalDialog
    val currentUser = settingsStateHolder.currentUser
    val isLoading = settingsStateHolder.isLoading

    init {
        initializeCurrentUser()
        loadUserSettings()
    }

    private fun initializeCurrentUser() {
        settingsStateHolder.updateCurrentUser(getCurrentUser())
    }

    private fun loadUserSettings() {
        viewModelScope.launch {
            getCurrentUser()?.uid?.let { userId ->
                try {
                    userSettingsManager.loadUserSettings(userId) { enabled, timeStr ->
                        settingsStateHolder.updateNotificationEnabled(enabled)
                        setNotificationTime(timeStr)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading user settings", e)
                    toastManager.showGeneralErrorToast()
                }
            }
        }
    }

    private fun getCurrentUser() = Firebase.auth.currentUser

    private fun setNotificationTime(timeStr: String?) {
        timeStr?.let {
            try {
                val time = notificationManager.parseTime(timeStr)
                settingsStateHolder.updateNotificationTime(time)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing notification time", e)
                toastManager.showGeneralErrorToast()
            }
        }
    }

    fun updateNotificationSettings(enabled: Boolean) {
        viewModelScope.launch {
            getCurrentUser()?.uid?.let { userId ->
                try {
                    // 알림 설정을 Firestore와 Room에 저장
                    applyNotificationSettings(userId, enabled)

                    // 알림 활성화 상태에 따라 알람 스케줄링
                    if (enabled) {
                        notificationTime.value?.let { time ->
                            notificationManager.scheduleNotification(time.hour, time.minute)
                        }
                    } else {
                        notificationManager.cancelExistingAlarm()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating notification settings", e)
                    toastManager.showGeneralErrorToast()
                }
            } ?: Log.e(TAG, "Failed to update notification settings: User not logged in")
        }
    }

    private suspend fun applyNotificationSettings(userId: String, enabled: Boolean) {
        notificationManager.updateNotificationSettings(userId, enabled)
        settingsStateHolder.updateNotificationEnabled(enabled)
    }

    fun updateNotificationTime(newTime: LocalTime) {
        viewModelScope.launch {
            getCurrentUser()?.uid?.let { userId ->
                try {
                    // Firestore와 Room에 시간 업데이트
                    updateNotificationTimeForUser(userId, newTime)

                    // 알림이 활성화된 상태에서만 알람 재설정
                    if (notificationEnabled.value) {
                        notificationManager.scheduleNotification(newTime.hour, newTime.minute)
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

    private suspend fun updateNotificationTimeForUser(userId: String, newTime: LocalTime) {
        notificationManager.updateNotificationTime(userId, newTime)
        settingsStateHolder.updateNotificationTime(newTime)
    }

    fun logIn(onLogInSuccess: () -> Unit) {
        viewModelScope.launch {
            settingsStateHolder.updateIsLoading(true)
            try {
                if (getCurrentUser() != null) {
                    toastManager.showAlreadyLoggedInErrorToast()
                    return@launch
                }
                onLogInSuccess()
            } catch (e: Exception) {
                Log.e(TAG, "Error during login", e)
                toastManager.showGeneralErrorToast()
            } finally {
                settingsStateHolder.updateIsLoading(false)
            }
        }
    }

    fun logOut(onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                if (isUserLoggedOut()) return@launch

                performLogout()
                toastManager.showLogoutSuccessToast()
                onLogoutSuccess()
            } catch (e: Exception) {
                Log.e(TAG, "Error during logout", e)
                toastManager.showGeneralErrorToast()
            } finally {
                settingsStateHolder.updateIsLoading(false)
            }
        }
    }

    private fun isUserLoggedOut(): Boolean {
        if (getCurrentUser() == null) {
            toastManager.showAlreadyLoggedOutErrorToast()
            return true
        }
        return false
    }

    private suspend fun performLogout() {
        settingsStateHolder.updateIsLoading(true)
        authManager.clearCredentials()
        authStateHolder.setUnAuthenticated()
        scheduleAlarmUseCase.cancelExistingAlarm()
    }

    fun withdrawUser(onWithDrawlSuccess: () -> Unit, onReLogInRequired: () -> Unit) {
        viewModelScope.launch {
            settingsStateHolder.updateIsLoading(true)
            try {
                val user = getCurrentUser() ?: run {
                    toastManager.showLogInRequiredErrorToast()
                    return@launch
                }

                userSettingsManager.deleteUserData(user.uid)

                // 계정 삭제 시도
                Firebase.auth.currentUser?.let { currentUser ->
                    try {
                        currentUser.delete().await()
                        toastManager.showWithDrawlSuccessToast()
                        onWithDrawlSuccess()
                    } catch (e: FirebaseAuthRecentLoginRequiredException) {
                        toastManager.showReLoginForWithDrawlToast()
                        Firebase.auth.signOut()
                        onReLogInRequired()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during withdrawal", e)
                toastManager.showGeneralErrorToast()
            } finally {
                settingsStateHolder.updateIsLoading(false)
            }
        }
    }

    fun createEmailIntent(): Intent? {
        return try {
            emailManager.createEmailIntent()
        } catch (e: URISyntaxException) {
            Log.e(TAG, "Invalid URI syntax", e)
            toastManager.showURISyntaxException()
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error creating email intent", e)
            toastManager.showGeneralErrorToast()
            null
        }
    }

    fun updateShowWithdrawalDialog(show: Boolean) {
        viewModelScope.launch {
            settingsStateHolder.updateShowWithdrawalDialog(show)
        }
    }
}