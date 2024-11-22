package com.example.passionDaily.ui.viewmodels.oauth.google

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.data.local.entity.UserEntity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoogleAuthViewModel @Inject constructor(
    private val authRepository: GoogleAuthRepository
) : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState = _authState.asStateFlow()

    init {
        checkCurrentuser()
    }

    private fun checkCurrentuser() {
        viewModelScope.launch {
            Firebase.auth.currentUser?.let { user ->
                _authState.value
            }
        }
    }
}