package com.example.passionDaily.ui.viewmodels

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.R
import com.example.passionDaily.data.repository.local.LocalFavoriteRepository
import com.example.passionDaily.data.repository.local.LocalQuoteCategoryRepository
import com.example.passionDaily.data.repository.local.LocalQuoteRepository
import com.example.passionDaily.data.repository.local.LocalUserRepository
import com.example.passionDaily.data.repository.remote.RemoteUserRepository
import com.example.passionDaily.manager.AuthenticationManager
import com.example.passionDaily.manager.SettingsManager
import com.example.passionDaily.resources.StringProvider
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
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

    init {
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
                safeSettingsOperation {
                    settingsManager.updateNotificationSettings(userId, enabled)
                    _notificationEnabled.emit(enabled)
                }
            }
        }
    }

    fun updateNotificationTime(newTime: LocalTime) {
        viewModelScope.launch {
            getCurrentUser()?.uid?.let { userId ->
                safeSettingsOperation {
                    settingsManager.updateNotificationTime(userId,newTime)
                    _notificationTime.emit(newTime)
                }
            }
        }
    }

    fun logIn() {
        viewModelScope.launch {
            safeSettingsOperation {
                if (getCurrentUser() != null) {
                    _toastMessage.emit(stringProvider.getString(R.string.error_already_logged_in))
                    return@safeSettingsOperation
                }
                _navigateToLogin.emit(true)
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

                Firebase.auth.signOut()
                _toastMessage.emit(stringProvider.getString(R.string.success_logout))
                _navigateToQuote.emit(true)
            }
        }
    }

    fun withdrawUser() {
        viewModelScope.launch {
            safeSettingsOperation {
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
            val errorMessage = mapExceptionToErrorMessage(e)
            Log.e(TAG, "Error in settings operation", e)
            _toastMessage.emit(errorMessage)
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
                stringProvider.getString(R.string.error_unexpected, e.message.orEmpty())
        }
    }
}