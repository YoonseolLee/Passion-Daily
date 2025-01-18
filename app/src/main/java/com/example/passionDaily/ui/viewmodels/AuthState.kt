package com.example.passionDaily.ui.viewmodels

sealed class AuthState {
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Authenticated(val userId: String) : AuthState()
    data class RequiresConsent(val userId: String, val userProfileJson: String?) : AuthState()
    data class Error(val message: String) : AuthState()
}