package com.example.passionDaily.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.auth.GoogleAuthClient
import com.example.passionDaily.auth.SignInResult
import com.example.passionDaily.util.LoginState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val googleAuthClient: GoogleAuthClient
) : ViewModel() {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState = _loginState.asStateFlow()

    fun signInWithGoogle() {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            try {
                val result = googleAuthClient.signIn()

                _loginState.value = when (result) {
                    is SignInResult.Success -> LoginState.Success
                    is SignInResult.Error -> LoginState.Error(result.message)
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}