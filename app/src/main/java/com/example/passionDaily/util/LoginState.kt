package com.example.passionDaily.util

import com.example.passionDaily.data.remote.model.user.User

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: User?) : LoginState()
    data class Error(val message: String) : LoginState()
}