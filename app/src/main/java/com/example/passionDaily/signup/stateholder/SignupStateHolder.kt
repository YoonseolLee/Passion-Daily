package com.example.passionDaily.signup.stateholder

import com.example.passionDaily.signup.domain.model.LoginFormState
import kotlinx.coroutines.flow.StateFlow

interface SignupStateHolder {
    val loginFormState: StateFlow<LoginFormState>
    val isLoading: StateFlow<Boolean>

    fun updateEmail(email: String)
    suspend fun updateIsLoading(isLoading: Boolean)
    fun updateFormState(newState: LoginFormState)
}
