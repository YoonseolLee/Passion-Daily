package com.example.passionDaily.ui.viewmodels

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.data.local.dao.UserDao
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao
) : ViewModel() {

    private val _notificationEnabled = MutableStateFlow(false)
    val notificationEnabled: StateFlow<Boolean> = _notificationEnabled.asStateFlow()

    private val _notificationTime = MutableStateFlow<LocalTime?>(null)
    val notificationTime: StateFlow<LocalTime?> = _notificationTime.asStateFlow()

    private val _navigateToQuote = MutableStateFlow(false)
    val navigateToQuote: StateFlow<Boolean> = _navigateToQuote.asStateFlow()

    private val _navigateToLogin = MutableStateFlow(false)
    val navigateToLogin: StateFlow<Boolean> = _navigateToLogin.asStateFlow()

    init {
        viewModelScope.launch {
            val userId = Firebase.auth.currentUser?.uid ?: return@launch

            userDao.getUserByUserId(userId)?.let { user ->
                _notificationEnabled.value = user.notificationEnabled

                user.notificationTime?.let { timeStr ->
                    try {
                        val time = LocalTime.parse(timeStr)
                        _notificationTime.value = time
                    } catch (e: Exception) {
                        Log.e("SettingsViewModel", "Error parsing time", e)
                    }
                }
            }
        }
    }

    fun updateNotificationSettings(enabled: Boolean) {
        viewModelScope.launch {
            val userId = Firebase.auth.currentUser?.uid ?: return@launch

            // Firestore에 저장
            firestore.collection("users")
                .document(userId)
                .update("notificationEnabled", enabled)

            // Room에 저장
            userDao.updateNotificationSetting(userId, enabled)
            _notificationEnabled.value = enabled
        }
    }

    fun updateNotificationTime(newTime: LocalTime) {
        viewModelScope.launch {
            val userId = Firebase.auth.currentUser?.uid ?: return@launch

            // Firestore에 저장
            firestore.collection("users")
                .document(userId)
                .update("notificationTime", newTime.toString())

            // Room에 저장
            userDao.updateNotificationTime(userId, newTime.toString())
            _notificationTime.value = newTime
        }
    }

    fun logIn(context: Context) {
        viewModelScope.launch {
            try {
                if (Firebase.auth.currentUser != null) {
                    Toast.makeText(context, "이미 로그인 되어 있습니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                _navigateToLogin.value = true
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "로그인 실패", e)
            }
        }
    }

    fun logOut(context: Context) {
        viewModelScope.launch {
            try {
                if (Firebase.auth.currentUser == null) {
                    Toast.makeText(context, "이미 로그아웃 되어 있습니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                Firebase.auth.signOut()
                Toast.makeText(context, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
                _navigateToQuote.value = true
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "로그아웃 실패", e)
            }
        }
    }

    fun onNavigatedToQuote() {
        _navigateToQuote.value = false
    }

    fun onNavigatedToLogin() {
        _navigateToLogin.value = false
    }
}