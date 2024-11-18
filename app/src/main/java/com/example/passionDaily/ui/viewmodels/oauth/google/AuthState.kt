package com.example.passionDaily.ui.viewmodels.oauth.google

import com.example.passionDaily.data.local.entity.UserEntity

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: UserEntity) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}