package com.example.passionDaily.login.stateholder

import com.example.passionDaily.login.domain.model.LoginFormState
import com.example.passionDaily.login.state.AuthState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

interface LoginStateHolder {

    val authState: StateFlow<AuthState>
    val isLoggedIn: StateFlow<Boolean>
    val isLoading: StateFlow<Boolean>
    val loginFormState: StateFlow<LoginFormState>

    suspend fun updateAuthState(authState: AuthState)
    suspend fun updateIsLoggedIn(isLoggedIn: Boolean)
    suspend fun updateIsLoading(isLoading: Boolean)
    suspend fun clearLoginState()
    fun updateEmail(email: String)
    fun updateFormState(newState: LoginFormState)
}