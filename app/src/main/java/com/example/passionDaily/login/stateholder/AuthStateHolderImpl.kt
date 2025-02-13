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
        Log.d("AuthDebug", "Setting authenticated state for userId: $userId")
        _authState.emit(AuthState.Authenticated(userId))
    }

    override suspend fun setUnAuthenticated() {
        _authState.emit(AuthState.Unauthenticated)
        Log.d("AuthStateHolder", "User unauthenticated")
    }

    override suspend fun setRequiresConsent(userId: String, userProfileJson: String?) {
        _authState.emit(AuthState.RequiresConsent(userId, userProfileJson))
        Log.d("AuthStateHolder", "User requires consent with ID: $userId")
    }
}