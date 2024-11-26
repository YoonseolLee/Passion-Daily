package com.example.passionDaily.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.auth.GoogleAuthUser
import com.example.passionDaily.data.repository.local.UserRepository
import com.example.passionDaily.util.SignUpState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val loginViewModel: LoginViewModel
) : ViewModel() {

    private val _signUpState = MutableStateFlow<SignUpState>(SignUpState.Idle)
    val signUpState = _signUpState.asStateFlow()

    fun signUp(user: GoogleAuthUser) {
        viewModelScope.launch {
            _signUpState.value = SignUpState.Loading
            val result = userRepository.createUser(user)

            if (result.isSuccess) {
                _signUpState.value = SignUpState.Success
                loginViewModel.onSignUpSuccess(user) // 회원가입 성공 후 로그인 상태로 전환
            } else {
                _signUpState.value = SignUpState.Error("Sign up failed")
            }
        }
    }
}
