package com.example.passionDaily.ui.state

import com.example.passionDaily.ui.viewmodels.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthStateHolder @Inject constructor() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState = _authState.asStateFlow()

    suspend fun setUnauthenticated() {
        _authState.emit(AuthState.Unauthenticated)
    }

    suspend fun setAuthenticated(userId: String) {
        _authState.emit(AuthState.Authenticated(userId))
    }

    suspend fun setRequiresConsent(userId: String, userProfileJson: String?) {
        _authState.emit(AuthState.RequiresConsent(userId, userProfileJson))
    }

    suspend fun setError(message: String) {
        _authState.emit(AuthState.Error(message))
    }
}