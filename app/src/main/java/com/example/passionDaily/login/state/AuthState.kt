package com.example.passionDaily.login.state

sealed class AuthState {
    data class Authenticated(val userId: String) : AuthState()
    object Unauthenticated : AuthState()
    data class RequiresConsent(val userId: String, val userProfileJson: String?) : AuthState()
}