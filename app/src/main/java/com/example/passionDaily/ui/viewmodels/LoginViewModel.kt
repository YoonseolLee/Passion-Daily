package com.example.passionDaily.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.auth.GoogleAuthClient
import com.example.passionDaily.auth.GoogleAuthUser
import com.example.passionDaily.util.LoginState
import com.example.passionDaily.util.SignUpState
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
            val result = googleAuthClient.signIn()

            _loginState.value = when (result) {
                is SignUpState.Success -> {
                    LoginState.Success(user = result.user)
                }

                is SignUpState.Error -> {
                    LoginState.Error(result.message)
                }

                else -> {
                    LoginState.Idle
                }
            }
        }
    }

    // 회원가입 성공 처리
    fun onSignUpSuccess(user: GoogleAuthUser?) {
        _loginState.value = LoginState.Success(user)
    }

    fun clearLoginAction() {
        _loginState.value = null
    }
}