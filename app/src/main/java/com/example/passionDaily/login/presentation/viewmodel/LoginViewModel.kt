package com.example.passionDaily.login.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.login.manager.UrlManager
import com.example.passionDaily.login.manager.UserConsentManager
import com.example.passionDaily.login.stateholder.AuthStateHolder
import com.example.passionDaily.signup.domain.model.LoginVerification
import com.example.passionDaily.signup.manager.SignupManager
import com.example.passionDaily.signup.manager.VerifyManager
import com.example.passionDaily.signup.stateholder.SignupStateHolder
import com.example.passionDaily.toast.manager.ToastManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userConsentManager: UserConsentManager,
    private val authStateHolder: AuthStateHolder,
    private val urlManager: UrlManager,
    private val signupStateHolder: SignupStateHolder,
    private val verifyManager: VerifyManager,
    private val toastManager: ToastManager,
    private val signupManager: SignupManager
) : ViewModel() {

    val consent = userConsentManager.consent
    val isAgreeAllChecked = userConsentManager.isAgreeAllChecked
    val isLoading = signupStateHolder.isLoading
    val authState = authStateHolder.authState
    val signupFormState = signupStateHolder.loginFormState

    fun signup() {
        viewModelScope.launch {
            val currentState = signupFormState.value

            val result = verifyManager.verifyLoginForm(
                currentState.email
            )

            signupStateHolder.updateFormState(result.formState)

            when (result.verification) {
                is LoginVerification.Success.ExistingUser -> {
                    // 기존 회원 -> 로그인 링크 전송
                    signupManager.sendSignInLinkToEmail(currentState.email)
                }
                is LoginVerification.Success.NewUser -> {
                    // 새 회원 -> 회원가입 진행
                    authStateHolder.setRequiresConsent()

                }
                is LoginVerification.Error.InvalidEmailFormat -> {
                    toastManager.showInvalidEmailFormatToast()
                }
                is LoginVerification.Error.ServerError -> {
                    toastManager.showGeneralErrorToast()
                }
            }
        }
    }

    fun updateEmail(email: String) {
        signupStateHolder.updateEmail(email)
    }
}