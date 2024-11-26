package com.example.passionDaily.util

import com.example.passionDaily.auth.GoogleAuthUser

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: GoogleAuthUser?) : LoginState()
    data class Error(val message: String) : LoginState()
}
