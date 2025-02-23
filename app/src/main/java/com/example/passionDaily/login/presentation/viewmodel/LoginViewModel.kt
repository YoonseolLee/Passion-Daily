package com.example.passionDaily.login.presentation.viewmodel

import android.content.Context
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.login.manager.UrlManager
import com.example.passionDaily.login.manager.UserConsentManager
import com.example.passionDaily.login.manager.UserProfileManager
import com.example.passionDaily.login.stateholder.AuthStateHolder
import com.example.passionDaily.login.stateholder.LoginStateHolder
import com.example.passionDaily.login.domain.model.LoginFormState
import com.example.passionDaily.login.domain.model.LoginVerification
import com.example.passionDaily.login.domain.model.VerificationResult
import com.example.passionDaily.login.manager.SignupManager
import com.example.passionDaily.login.manager.VerifyManager
import com.example.passionDaily.toast.manager.ToastManager
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userConsentManager: UserConsentManager,
    private val authStateHolder: AuthStateHolder,
    private val urlManager: UrlManager,
    private val signupManager: SignupManager,
    private val verifyManager: VerifyManager,
    private val toastManager: ToastManager,
    private val userProfileManager: UserProfileManager,
    private val loginStateHolder: LoginStateHolder,
) : ViewModel() {

    val consent = userConsentManager.consent
    val isAgreeAllChecked = userConsentManager.isAgreeAllChecked
    val isLoading = loginStateHolder.isLoading
    val authState = authStateHolder.authState
    val signupFormState = loginStateHolder.loginFormState
    val currentState = signupFormState.value
    private val _showEmailSentDialog = MutableStateFlow(false)
    val showEmailSentDialog = _showEmailSentDialog.asStateFlow()


    fun signup() {
        viewModelScope.launch {
            val currentState = signupFormState.value
            val result = verifyLoginForm(currentState.email)
            loginStateHolder.updateFormState(result.formState)
            handleVerificationResult(result.verification)
        }
    }

    private suspend fun verifyLoginForm(email: String): VerificationResult {
        return verifyManager.verifyLoginForm(email)
    }

    private suspend fun handleVerificationResult(verification: LoginVerification) {
        when (verification) {
            is LoginVerification.Success.ExistingUser -> {
                // 기존 회원 -> 로그인 링크 전송
                signupManager.sendSignInLinkToEmail(currentState.email)
                _showEmailSentDialog.value = true  // 이메일 전송 성공시 다이얼로그 표시
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

    fun updateEmail(email: String) {
        loginStateHolder.updateEmail(email)
    }

    fun toggleAgreeAll() {
        userConsentManager.toggleAgreeAll()
    }

    fun toggleIndividualItem(item: String) {
        userConsentManager.toggleIndividualItem(item)
    }

    fun onNextButtonClick() {
        viewModelScope.launch {
            loginStateHolder.updateIsLoading(true)  // 즉시 로딩 표시
            handleNextClick()
        }
    }

    private suspend fun handleNextClick() {
        if (!consent.value.isAllAgreed) {
            loginStateHolder.updateIsLoading(false)
            return
        }

        val email = signupFormState.value.email
        if (email.isBlank()) {
            loginStateHolder.updateIsLoading(false)
            toastManager.showInvalidEmailFormatToast()
            return
        }

        try {
            withContext(Dispatchers.IO) {
                signupManager.sendSignInLinkToEmail(email)
            }
            _showEmailSentDialog.value = true
        } catch (e: Exception) {
            handleException(e)
        } finally {
            loginStateHolder.updateIsLoading(false)
        }
    }

    fun dismissEmailSentDialog() {
        _showEmailSentDialog.value = false
    }

    private suspend fun saveUserProfile(json: String) {
        try {
            userProfileManager.saveUserToRoom(json)
            userProfileManager.saveUserToFirestore(json)
        } catch (e: Exception) {
            handleException(e)
        }
    }

    private suspend fun setAuthenticated(userId: String) {
        try {
            userProfileManager.setAuthenticated(userId)
        } catch (e: Exception) {
            handleException(e)
        }
    }

    fun openUrl(context: Context, url: String) {
        urlManager.openUrl(context, url)
    }

    fun handleException(e: Exception) {
        showToastForException(e)
    }

    private fun showToastForException(e: Exception) {
        when (e) {
            is GetCredentialCancellationException -> {
                toastManager.showNetworkErrorToast()
            }

            is IOException -> toastManager.showNetworkErrorToast()
            is FirebaseAuthInvalidCredentialsException -> toastManager.showCredentialErrorToast()
            is IllegalArgumentException, is JSONException -> toastManager.showGeneralErrorToast()
            is FirebaseFirestoreException -> toastManager.showFirebaseErrorToast()
            else -> toastManager.showGeneralErrorToast()
        }
    }
}