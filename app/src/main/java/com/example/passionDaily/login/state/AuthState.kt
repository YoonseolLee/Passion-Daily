package com.example.passionDaily.login.state

sealed class AuthState {
    data class Authenticated(val userId: String) : AuthState()
    object Unauthenticated : AuthState()
    object RequiresConsent : AuthState()
}