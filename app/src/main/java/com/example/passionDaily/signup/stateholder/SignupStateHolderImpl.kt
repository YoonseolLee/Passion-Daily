package com.example.passionDaily.signup.stateholder

import com.example.passionDaily.signup.domain.model.LoginFormState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignupStateHolderImpl @Inject constructor(): SignupStateHolder {
    private val _loginFormState = MutableStateFlow(LoginFormState())
    override val loginFormState = _loginFormState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    override fun updateEmail(email: String) {
        _loginFormState.update { it.copy(email = email) }
    }

    override suspend fun updateIsLoading(isLoading: Boolean) {
        _isLoading.emit(isLoading)
    }

    override fun updateFormState(newState: LoginFormState) {
        _loginFormState.update { newState }
    }
}