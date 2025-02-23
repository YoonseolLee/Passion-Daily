package com.example.passionDaily.login.stateholder

import com.example.passionDaily.login.domain.model.LoginFormState
import com.example.passionDaily.login.state.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginStateHolderImpl @Inject constructor() : LoginStateHolder {

    // 로그인 상태
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // 로그인 여부
    private val _isLoggedIn = MutableStateFlow(false)
    override val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // 로딩 상태
    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loginFormState = MutableStateFlow(LoginFormState())
    override val loginFormState = _loginFormState.asStateFlow()

    override suspend fun updateAuthState(authState: AuthState) {
        _authState.emit(authState)
    }

    override suspend fun updateIsLoggedIn(isLoggedIn: Boolean) {
        _isLoggedIn.value = isLoggedIn
    }

    override suspend fun updateIsLoading(isLoading: Boolean) {
        _isLoading.emit(isLoading)
    }

    override suspend fun clearLoginState() {
        _isLoggedIn.value = false
        _isLoading.value = false
    }

    override fun updateEmail(email: String) {
        _loginFormState.update { it.copy(email = email) }
    }

    override fun updateFormState(newState: LoginFormState) {
        _loginFormState.update { newState }
    }
}