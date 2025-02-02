package com.example.passionDaily.settings.presentation.viewmodel

import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.constants.ViewModelConstants.Settings.TAG
import com.example.passionDaily.login.manager.AuthenticationManager
import com.example.passionDaily.settings.manager.SettingsManager
import com.example.passionDaily.manager.alarm.DailyQuoteAlarmScheduler
import com.example.passionDaily.resources.StringProvider
import com.example.passionDaily.login.stateholder.AuthStateHolder
import com.example.passionDaily.settings.manager.EmailManager
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
    private val settingsManager: SettingsManager,
    private val stringProvider: StringProvider,
    private val alarmScheduler: DailyQuoteAlarmScheduler,
    private val authManager: AuthenticationManager,
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
        settingsStateHolder.updateCurrentUser(getCurrentUser())

        viewModelScope.launch {
            getCurrentUser()?.uid?.let { userId ->
                try {
                    settingsManager.loadUserSettings(userId) { enabled, timeStr ->
                        settingsStateHolder.updateNotificationEnabled(enabled)
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
                Log.d(
                    TAG,
                    "Attempting to update notification settings: enabled=$enabled for user=$userId"
                )

                try {
                    // Firestore와 Room에 설정 업데이트
                    settingsManager.updateNotificationSettings(userId, enabled)
                    Log.d(TAG, "Successfully updated notification settings in databases")

                    settingsStateHolder.updateNotificationEnabled(enabled)
                    Log.d(TAG, "Updated local notification enabled state")

                    // 알람 관리
                    if (enabled) {
                        notificationTime.value?.let { time ->
                            Log.d(
                                TAG,
                                "Notification enabled, scheduling alarm for ${time.hour}:${time.minute}"
                            )
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
                Log.d(
                    TAG,
                    "Attempting to update notification time to ${newTime.hour}:${newTime.minute} for user=$userId"
                )

                try {
                    // Firestore와 Room에 시간 업데이트
                    settingsManager.updateNotificationTime(userId, newTime)
                    Log.d(TAG, "Successfully updated notification time in databases")

                    settingsStateHolder.updateNotificationTime(newTime)
                    Log.d(TAG, "Updated local notification time state")

                    // 알림이 활성화된 상태에서만 알람 재설정
                    if (notificationEnabled.value) {
                        Log.d(
                            TAG,
                            "Notifications are enabled, rescheduling alarm for new time ${newTime.hour}:${newTime.minute}"
                        )
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
                if (getCurrentUser() == null) {
                    toastManager.showAlreadyLoggedOutErrorToast()
                    return@launch
                }

                settingsStateHolder.updateIsLoading(true)
                authManager.clearCredentials()
                authStateHolder.setUnAuthenticated()
                alarmScheduler.cancelExistingAlarm()

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

    fun withdrawUser(onWithDrawlSuccess: () -> Unit, onReLogInRequired: () -> Unit) {
        viewModelScope.launch {
            settingsStateHolder.updateIsLoading(true)
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