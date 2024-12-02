package com.example.passionDaily.ui.viewmodels

import com.example.passionDaily.data.remote.model.user.User

// 통합된 인증 상태
sealed class AuthState {
    object Loading : AuthState()
    data class SignedIn(val user: User) : AuthState()
    object SignUpRequired : AuthState()
    data class Error(val message: String) : AuthState()
    object SignedOut : AuthState()
}
