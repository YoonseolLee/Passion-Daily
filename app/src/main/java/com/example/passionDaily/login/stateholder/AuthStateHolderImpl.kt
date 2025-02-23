package com.example.passionDaily.login.stateholder

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton
import com.example.passionDaily.login.state.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class AuthStateHolderImpl @Inject constructor() : AuthStateHolder {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    override suspend fun setAuthenticated(userId: String) {
        _authState.emit(AuthState.Authenticated(userId))
    }

    override suspend fun setUnAuthenticated() {
        _authState.emit(AuthState.Unauthenticated)
    }

    override suspend fun setRequiresConsent() {
        _authState.emit(AuthState.RequiresConsent)
    }
}