package com.example.passionDaily.login.stateholder

import com.example.passionDaily.login.state.AuthState
import kotlinx.coroutines.flow.StateFlow

interface AuthStateHolder {
    val authState: StateFlow<AuthState>

    suspend fun setAuthenticated(userId: String)
    suspend fun setUnAuthenticated()
    suspend fun setRequiresConsent(userId: String, userProfileJson: String?)
}