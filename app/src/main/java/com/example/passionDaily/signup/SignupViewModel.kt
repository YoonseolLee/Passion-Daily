package com.example.passionDaily.signup

import android.content.Context
import android.util.Log
import androidx.credentials.exceptions.GetCredentialCancellationException
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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.json.JSONException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
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

    // 전체 동의 토글 메서드
    fun toggleAgreeAll() {
        userConsentManager.toggleAgreeAll()
    }

    // 개별 항목 토글 메서드
    fun toggleIndividualItem(item: String) {
        userConsentManager.toggleIndividualItem(item)
    }

    fun signup() {
        viewModelScope.launch {
            updateIsLoading(true)
            try {
                val currentState = signupFormState.value

                val result = verifyManager.verifyLoginForm(
                    currentState.email
                )

                signupStateHolder.updateFormState(result.formState)

                when (result.verification) {
                    is LoginVerification.Success.ExistingUser -> {
                        // 기존 회원 -> 로그인 링크 전송
                        signupManager.sendSignInLinkToEmail(currentState.email)
                        // TODO: 링크 재섭속 성공 시 setAuthenticated로 변경
//                    authStateHolder.setAuthenticated(userId = )
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
            } catch (e: Exception) {
                handleException(e)
            }
            finally {
                updateIsLoading(false)
            }
        }
    }

    fun openUrl(context: Context, url: String) {
        urlManager.openUrl(context, url)
    }

    fun updateEmail(email: String) {
        signupStateHolder.updateEmail(email)
    }

    suspend fun updateIsLoading(isLoading: Boolean) {
        signupStateHolder.updateIsLoading(isLoading)
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