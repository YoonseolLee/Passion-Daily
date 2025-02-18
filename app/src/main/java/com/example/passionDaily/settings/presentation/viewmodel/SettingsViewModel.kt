package com.example.passionDaily.settings.presentation.viewmodel

import android.content.Intent
import android.database.sqlite.SQLiteException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.constants.ViewModelConstants.Favorites
import com.example.passionDaily.favorites.stateholder.FavoritesStateHolder
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
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URISyntaxException
import java.time.LocalTime
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

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
    private val userConsentManager: UserConsentManager,
    private val favoritesStateHolder: FavoritesStateHolder
) : ViewModel(), SettingsViewModelActions, SettingsViewModelState {

    override val notificationEnabled = settingsStateHolder.notificationEnabled
    override val notificationTime = settingsStateHolder.notificationTime
    override val showWithdrawalDialog = settingsStateHolder.showWithdrawalDialog
    override val currentUser = settingsStateHolder.currentUser
    override val isLoading = settingsStateHolder.isLoading

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        settingsStateHolder.updateCurrentUser(auth.currentUser)

        // 로그인된 경우 사용자 설정 로드
        auth.currentUser?.let { user ->
            loadUserSettings()
        } ?: run {
            // 로그아웃된 경우 설정 초기화
            settingsStateHolder.updateNotificationEnabled(false)
            settingsStateHolder.updateNotificationTime(null)
        }

    }

    init {
        // Firebase Auth 상태 변경을 감지하여 currentUser 업데이트
        Firebase.auth.addAuthStateListener(authStateListener)
    }

    override fun onCleared() {
        super.onCleared()
        Firebase.auth.removeAuthStateListener(authStateListener)
        viewModelScope.cancel() // 모든 코루틴 작업 취소
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
                    handleError(e)
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
                handleError(e)
            }
        }
    }

    override fun updateNotificationSettings(enabled: Boolean) {
        viewModelScope.launch {
            getCurrentUser()?.uid?.let { userId ->
                try {
                    notificationManager.updateNotificationSettingsToFirestore(userId, enabled)
                        .also {
                            notificationManager.updateNotificationSettingsToRoom(userId, enabled)
                        }.also {
                            settingsStateHolder.updateNotificationEnabled(enabled)
                        }

                    // 알림 활성화 상태에 따라 알람 스케줄링
                    if (enabled) {
                        notificationTime.value?.let { time ->
                            notificationManager.scheduleNotification(time.hour, time.minute)
                        }
                    } else {
                        notificationManager.cancelExistingAlarm()
                    }
                } catch (e: Exception) {
                    handleError(e)
                }
            } ?: run { /* User not logged in */ }
        }
    }


    override fun updateNotificationTime(newTime: LocalTime) {
        viewModelScope.launch {
            getCurrentUser()?.uid?.let { userId ->
                try {
                    notificationManager.updateNotificationTimeToFirestore(userId, newTime)
                        .also {
                            // Firestore 업데이트가 완료된 후 Room 업데이트
                            notificationManager.updateNotificationTimeToRoom(userId, newTime)
                        }
                        .also {
                            // Room 업데이트가 완료된 후 Settings 업데이트
                            settingsStateHolder.updateNotificationTime(newTime)
                        }

                    // 알림이 활성화된 상태에서만 알람 재설정
                    if (notificationEnabled.value) {
                        notificationManager.scheduleNotification(newTime.hour, newTime.minute)
                    }
                } catch (e: Exception) {
                    handleError(e)
                }
            } ?: run { /* User not logged in */ }
        }
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
                handleError(e)
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
                handleError(e)
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
                handleError(e)
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
        favoritesStateHolder.clearOptimisticFavorites()
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
            toastManager.showURISyntaxException()
            null
        } catch (e: Exception) {
            handleError(e)
            null
        }
    }

    override fun updateShowWithdrawalDialog(show: Boolean) {
        viewModelScope.launch {
            settingsStateHolder.updateShowWithdrawalDialog(show)
        }
    }

    private fun handleError(e: Exception) {
        when (e) {
            is IOException, is FirebaseNetworkException -> {
                toastManager.showNetworkErrorToast()
            }

            is FirebaseFirestoreException -> {
                toastManager.showFirebaseErrorToast()
            }

            is SQLiteException -> {
                toastManager.showRoomDatabaseErrorToast()
            }

            is IllegalStateException -> {
                toastManager.showGeneralErrorToast()
            }

            else -> {
                toastManager.showGeneralErrorToast()
            }
        }
    }
}