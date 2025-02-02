package com.example.passionDaily.login.stateholder

import com.example.passionDaily.login.state.AuthState
import kotlinx.coroutines.flow.StateFlow

interface LoginStateHolder {

    val authState: StateFlow<AuthState>
    val userProfileJson: StateFlow<String?>
    val userProfileJsonV2: StateFlow<String?>
    val isLoggedIn: StateFlow<Boolean>
    val isLoading: StateFlow<Boolean>

    suspend fun updateAuthState(authState: AuthState)
    suspend fun updateUserProfileJson(json: String?)
    suspend fun updateUserProfileJsonV2(json: String?)
    suspend fun updateIsLoggedIn(isLoggedIn: Boolean)
    suspend fun updateIsLoading(isLoading: Boolean)
}