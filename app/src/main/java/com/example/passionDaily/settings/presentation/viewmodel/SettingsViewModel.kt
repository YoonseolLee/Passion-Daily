package com.example.passionDaily.settings.presentation.viewmodel

import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.constants.ViewModelConstants.Settings.TAG
import com.example.passionDaily.login.manager.AuthenticationManager
import com.example.passionDaily.login.manager.UserConsentManager
import com.example.passionDaily.settings.manager.UserSettingsManager
import com.example.passionDaily.notification.usecase.ScheduleDailyQuoteAlarmUseCase
import com.example.passionDaily.login.stateholder.AuthStateHolder
import com.example.passionDaily.login.stateholder.LoginStateHolder
import com.example.passionDaily.settings.base.SettingsViewModelActions
import com.example.passionDaily.settings.base.SettingsViewModelState
import com.example.passionDaily.settings.manager.EmailManager
import com.example.passionDaily.settings.manager.NotificationManager
import com.example.passionDaily.settings.stateholder.SettingsStateHolder
import com.example.passionDaily.toast.manager.ToastManager
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
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
    private val settingsStateHolder: SettingsStateHolder,
    private val loginStateHolder: LoginStateHolder,
    private val userConsentManager: UserConsentManager
) : ViewModel(), SettingsViewModelActions, SettingsViewModelState {

    override val notificationEnabled = settingsStateHolder.notificationEnabled
    override val notificationTime = settingsStateHolder.notificationTime
    override val showWithdrawalDialog = settingsStateHolder.showWithdrawalDialog
    override val currentUser = settingsStateHolder.currentUser
    override val isLoading = settingsStateHolder.isLoading

    init {
        initializeCurrentUser()
        loadUserSettings()
    }

    private fun initializeCurrentUser() {
        settingsStateHolder.updateCurrentUser(getCurrentUser())
    }

    override fun loadUserSettings() {
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

    override fun updateNotificationSettings(enabled: Boolean) {
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

    override fun updateNotificationTime(newTime: LocalTime) {
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

    override fun logIn(onLogInSuccess: () -> Unit) {
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

    /**
     * onLogoutSuccess -> onNavigateToQuote
     */
    override fun logOut(onLogoutSuccess: () -> Unit) {
        settingsStateHolder.updateIsLoading(true)
        viewModelScope.launch {
            try {
                if (isUserLoggedOut()) return@launch

                clearUserData()
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

    private suspend fun isUserLoggedOut(): Boolean {
        return withContext(Dispatchers.IO) {
            val user = getCurrentUser()
            if (user == null) {
                toastManager.showAlreadyLoggedOutErrorToast()
                return@withContext true
            }
            false
        }
    }

    override fun withdrawUser(onWithdrawSuccess: () -> Unit, onReLogInRequired: () -> Unit) {
        viewModelScope.launch {
            settingsStateHolder.updateIsLoading(true)
            try {
                val user = getCurrentUser() ?: run {
                    toastManager.showLogInRequiredErrorToast()
                    return@launch
                }

                val deletionSuccess = attemptAccountDeletion(onWithdrawSuccess, onReLogInRequired)
                if (deletionSuccess) {
                    userSettingsManager.deleteUserData(user.uid)
                    clearUserData()
                    resetNotificationSettings()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during withdrawal", e)
                toastManager.showGeneralErrorToast()
            } finally {
                settingsStateHolder.updateIsLoading(false)
            }
        }
    }

    private suspend fun attemptAccountDeletion(
        onWithdrawSuccess: () -> Unit, onReLogInRequired: () -> Unit
    ): Boolean {
        return Firebase.auth.currentUser?.let { currentUser ->
            try {
                currentUser.delete().await()
                toastManager.showWithDrawlSuccessToast()
                onWithdrawSuccess()
                true
            } catch (e: FirebaseAuthRecentLoginRequiredException) {
                handleReLoginForWithdrawal(onReLogInRequired)
                false
            }
        } ?: false
    }

    private suspend fun clearUserData() {
        authManager.clearCredentials()
        userConsentManager.clearConsent()
        loginStateHolder.clearLoginState()
        authStateHolder.setUnAuthenticated()
        scheduleAlarmUseCase.cancelExistingAlarm()
    }

    private suspend fun handleReLoginForWithdrawal(onReLogInRequired: () -> Unit) {
        toastManager.showReLoginForWithDrawlToast()
        clearUserData()
        Firebase.auth.signOut()
        onReLogInRequired()
    }

    private fun resetNotificationSettings() {
        settingsStateHolder.updateNotificationEnabled(false)
        settingsStateHolder.updateNotificationTime(LocalTime.of(8, 0))
    }

    override fun createEmailIntent(): Intent? {
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

    override fun updateShowWithdrawalDialog(show: Boolean) {
        viewModelScope.launch {
            settingsStateHolder.updateShowWithdrawalDialog(show)
        }
    }
}