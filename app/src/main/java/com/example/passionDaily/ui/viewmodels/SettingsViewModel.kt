package com.example.passionDaily.ui.viewmodels

import android.util.Log
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
}