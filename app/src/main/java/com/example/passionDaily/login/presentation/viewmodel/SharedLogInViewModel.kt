package com.example.passionDaily.login.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.R
import com.example.passionDaily.constants.ViewModelConstants.SharedLogin.TAG
import com.example.passionDaily.user.data.remote.repository.RemoteUserRepository
import com.example.passionDaily.login.manager.AuthenticationManager
import com.example.passionDaily.toast.manager.ToastManager
import com.example.passionDaily.login.manager.UrlManager
import com.example.passionDaily.login.manager.UserConsentManager
import com.example.passionDaily.login.manager.UserProfileManager
import com.example.passionDaily.util.mapper.UserProfileMapper
import com.example.passionDaily.resources.StringProvider
import com.example.passionDaily.login.stateholder.AuthStateHolder
import com.example.passionDaily.login.stateholder.LoginStateHolder
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class SharedLogInViewModel @Inject constructor(
    private val authManager: AuthenticationManager,
    private val userProfileManager: UserProfileManager,
    private val userConsentManager: UserConsentManager,
    private val urlManager: UrlManager,
    private val remoteUserRepository: RemoteUserRepository,
    private val toastManager: ToastManager,
    private val userProfileMapper: UserProfileMapper,
    private val stringProvider: StringProvider,
    private val authStateHolder: AuthStateHolder,
    private val loginStateHolder: LoginStateHolder
) : ViewModel() {
    val authState = authStateHolder.authState

    val userProfileJson = loginStateHolder.userProfileJson
    val userProfileJsonV2 = loginStateHolder.userProfileJsonV2
    val isLoading = loginStateHolder.isLoading

    val consent = userConsentManager.consent
    val isAgreeAllChecked = userConsentManager.isAgreeAllChecked

//    private val _navigationEvents = Channel<NavigationEvent>()
//    val navigationEvents = _navigationEvents.receiveAsFlow()

    /**
     * LoginScreen
     */

    fun signInWithGoogle() {
        viewModelScope.launch {
            authManager.startLoading()
            try {
                // 기존 크리덴셜 클리어: 로그아웃 직후 재로그인 시, 자동 로그인 방지
                authManager.clearCredentials()

                // 새로운 크리덴셜 요청
                val result = authManager.getGoogleCredential()
                processSignInResult(result)
            } catch (e: Exception) {
                handleException(e)
            } finally {
                authManager.stopLoading()
            }
        }
    }

    private suspend fun processSignInResult(result: GetCredentialResponse) {
        val credential = result.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            try {
                val idToken = authManager.extractIdToken(credential)
                val authResult = authManager.authenticateWithFirebase(idToken)
                handleAuthResult(authResult)
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    private suspend fun handleAuthResult(authResult: AuthResult) {
        try {
            val firebaseUser = authManager.getFirebaseUser(authResult)
            val userId = authManager.getUserId(firebaseUser)
            val userProfileMap = userProfileManager.createInitialProfile(firebaseUser, userId)

            val userProfileJson = storeUserProfile(userProfileMap)
            handleUserRegistrationStatus(userId, userProfileJson)
        } catch (e: Exception) {
            handleException(e)
        }
    }

    private suspend fun storeUserProfile(userProfileMap: Map<String, Any?>): String {
        val userProfileJson = userProfileMapper.convertMapToJson(userProfileMap)
        authManager.updateUserProfileJson(userProfileJson)
        return userProfileJson
    }


    private suspend fun handleUserRegistrationStatus(userId: String, userProfileJson: String) {
        if (remoteUserRepository.isUserRegistered(userId)) {
            syncExistingUser(userId)
        } else {
            authStateHolder.setRequiresConsent(userId, userProfileJson)
        }
    }

    private suspend fun syncExistingUser(userId: String) {
        try {
            userProfileManager.syncExistingUser(userId)
        } catch (e: Exception) {
            handleException(e)
        }
    }

    /**
     * TermsConsentScreen
     */

    suspend fun verifyUserProfileJson(json: String?) {
        userProfileManager.verifyJson(json)
    }

    // 전체 동의 토글 메서드
    fun toggleAgreeAll() {
        userConsentManager.toggleAgreeAll()
    }

    // 개별 항목 토글 메서드
    fun toggleIndividualItem(item: String) {
        userConsentManager.toggleIndividualItem(item)
    }

    fun handleNextClick(userProfileJson: String?) {
        if (!consent.value.isAllAgreed) {
            Log.e(TAG, "User did not agree to required terms")
            return
        }

        viewModelScope.launch {
            try {
                userProfileJson?.let {
                    val updatedJson = userProfileManager.updateUserProfileWithConsent(
                        it,
                        consent.value
                    )

                    if (updatedJson != null) {
                        authManager.updateUserProfileJsonV2(updatedJson)
                        saveUserProfile(updatedJson)
                        toastManager.showLoginSuccessToast()
                    } else {
                        toastManager.showLoginErrorToast()
                    }
                } ?: run {
                    toastManager.showLoginSuccessToast()
                }
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    private suspend fun saveUserProfile(json: String) {
        try {
            userProfileManager.saveUserToRoom(json)
            userProfileManager.saveUserToFirestore(json)
        } catch (e: Exception) {
            handleException(e)
        }
    }

    fun openUrl(context: Context, url: String) {
        urlManager.openUrl(context, url)
    }

    /**
     * QuoteScreen
     */

    fun signalLoginSuccess() {
        viewModelScope.launch {
            try {
                authManager.updateIsLoggedIn(true)
                delay(100)
                toastManager.showLoginSuccessToast()
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    private fun handleException(e: Exception) {
        val errorMessage = e.message ?: stringProvider.getString(R.string.error_general)
        Log.e(TAG, errorMessage, e)

        when (e) {
            is GetCredentialCancellationException -> {
                // 사용자의 의도적인 취소이므로 에러 메시지를 표시하지 않음
                Log.d(TAG, "User cancelled the login process")
            }
            is IOException -> toastManager.showNetworkErrorToast()
            is FirebaseAuthInvalidCredentialsException -> toastManager.showCredentialErrorToast()
            is IllegalArgumentException, is JSONException -> toastManager.showGeneralErrorToast()
            is FirebaseFirestoreException -> toastManager.showFirebaseErrorToast()
            else -> toastManager.showGeneralErrorToast()
        }
    }
}
