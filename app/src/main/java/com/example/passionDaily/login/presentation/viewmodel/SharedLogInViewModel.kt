package com.example.passionDaily.login.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.CreateCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.R
import com.example.passionDaily.constants.ViewModelConstants.SharedLogin.TAG
import com.example.passionDaily.data.repository.remote.RemoteUserRepository
import com.example.passionDaily.login.manager.AuthenticationManager
import com.example.passionDaily.manager.ToastManager
import com.example.passionDaily.manager.UrlManager
import com.example.passionDaily.login.manager.UserConsentManager
import com.example.passionDaily.login.manager.UserProfileManager
import com.example.passionDaily.mapper.UserProfileMapper
import com.example.passionDaily.quote.domain.model.NavigationEvent
import com.example.passionDaily.resources.StringProvider
import com.example.passionDaily.login.stateholder.AuthStateHolder
import com.example.passionDaily.login.stateholder.LoginStateHolder
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.json.JSONException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class SharedLogInViewModel @Inject constructor(
    private val auth: FirebaseAuth,
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

    private val _navigationEvents = Channel<NavigationEvent>()
    val navigationEvents = _navigationEvents.receiveAsFlow()

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
            } catch (e: CreateCredentialCancellationException) {
                authStateHolder.setError(
                    e.message ?: stringProvider.getString(R.string.error_credential_retrieval)
                )
                toastManager.showLoginErrorMessage()
            } catch (e: IOException) {
                authStateHolder.setError(
                    e.message ?: stringProvider.getString(R.string.error_network_retry)
                )
                toastManager.showNetworkErrorToast()
            } catch (e: Exception) {
                authStateHolder.setError(
                    e.message ?: stringProvider.getString(R.string.error_general)
                )
                toastManager.showGeneralErrorToast()
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
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                authStateHolder.setError(
                    e.message
                        ?: stringProvider.getString(R.string.error_firebase_invalid_credential)
                )
                toastManager.showNetworkErrorToast()
            } catch (e: IOException) {
                authStateHolder.setError(
                    e.message ?: stringProvider.getString(R.string.error_network_retry)
                )
                toastManager.showNetworkErrorToast()
            } catch (e: Exception) {
                authStateHolder.setError(
                    e.message ?: stringProvider.getString(R.string.error_general)
                )
            }
        }
    }

    private suspend fun handleAuthResult(authResult: AuthResult) {
        try {
            val firebaseUser = authManager.getFirebaseUser(authResult)
            val userId = authManager.getUserId(firebaseUser)
            val userProfileMap = userProfileManager.createInitialProfile(firebaseUser, userId)

            storeUserProfile(userProfileMap)
            handleUserRegistrationStatus(userId)
        } catch (e: IllegalArgumentException) {
            authStateHolder.setError(e.message ?: stringProvider.getString(R.string.error_general))
            toastManager.showLoginErrorMessage()
        } catch (e: JSONException) {
            authStateHolder.setError(e.message ?: stringProvider.getString(R.string.error_general))
            toastManager.showLoginErrorMessage()
        } catch (e: Exception) {
            authStateHolder.setError(e.message ?: stringProvider.getString(R.string.error_general))
            toastManager.showGeneralErrorToast()
        }
    }

    private suspend fun storeUserProfile(userProfileMap: Map<String, Any?>) {
        val userProfileJson = userProfileMapper.convertMapToJson(userProfileMap)
        authManager.updateUserProfileJson(userProfileJson)
    }

    private suspend fun handleUserRegistrationStatus(userId: String) {
        if (remoteUserRepository.isUserRegistered(userId)) {
            syncExistingUser(userId)
        } else {
            authStateHolder.setRequiresConsent(userId, userProfileJson.value)
        }
    }

    private suspend fun syncExistingUser(userId: String) {
        try {
            userProfileManager.syncExistingUser(userId)
        } catch (e: IOException) {
            authStateHolder.setError(
                e.message ?: stringProvider.getString(R.string.error_network_retry)
            )
            toastManager.showNetworkErrorToast()
        } catch (e: FirebaseFirestoreException) {
            authStateHolder.setError(e.message ?: stringProvider.getString(R.string.error_general))
            toastManager.showFirebaseErrorToast()
        } catch (e: Exception) {
            authStateHolder.setError(e.message ?: stringProvider.getString(R.string.error_general))
        }
    }

    /**
     * TermsConsentScreen
     */

    fun verifyUserProfileJson(json: String?) {
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
                        showSignUpSuccessMessage()
                    } else {
                        showSignUpErrorMessage()
                    }
                } ?: run {
                    showSignUpErrorMessage()
                }
            } catch (e: Exception) {
                authStateHolder.setError(
                    e.message ?: stringProvider.getString(R.string.error_general)
                )
            }
        }
    }

    private suspend fun saveUserProfile(json: String) {
        try {
            userProfileManager.saveUserToRoom(json)
            userProfileManager.saveUserToFirestore(json, auth.currentUser)
        } catch (e: Exception) {
            authStateHolder.setError(e.message ?: stringProvider.getString(R.string.error_general))
        }
    }

    private fun showSignUpSuccessMessage() {
        toastManager.showLoginSuccessMessage()
    }

    private fun showSignUpErrorMessage() {
        toastManager.showLoginErrorMessage()
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
                showLoginSuccessMessage()
                _navigationEvents.send(NavigationEvent.NavigateToQuote)
            } catch (e: Exception) {
                authStateHolder.setError(
                    e.message ?: stringProvider.getString(R.string.error_general)
                )
            }
        }
    }

    fun signalLoginError(errorMessage: String) {
        viewModelScope.launch {
            try {
                authManager.updateIsLoggedIn(false)
                showLoginErrorMessage(errorMessage)
            } catch (e: Exception) {
                authStateHolder.setError(
                    e.message ?: stringProvider.getString(R.string.error_general)
                )
            }
        }
    }

    private fun showLoginSuccessMessage() {
        showSignUpSuccessMessage()
    }

    private fun showLoginErrorMessage(errorMessage: String) {
        showSignUpErrorMessage()
    }
}
