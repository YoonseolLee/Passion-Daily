package com.example.passionDaily.ui.viewmodels

import android.content.Context
import android.os.NetworkOnMainThreadException
import android.util.Log
import androidx.annotation.StringRes
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.R
import com.example.passionDaily.data.local.entity.UserEntity
import com.example.passionDaily.data.repository.local.UserRepository
import com.example.passionDaily.data.repository.remote.RemoteUserRepository
import com.example.passionDaily.manager.AuthenticationManager
import com.example.passionDaily.manager.ToastManager
import com.example.passionDaily.manager.UrlManager
import com.example.passionDaily.manager.UserConsentManager
import com.example.passionDaily.manager.UserProfileManager
import com.example.passionDaily.mapper.UserProfileMapper
import com.example.passionDaily.resources.StringProvider
import com.example.passionDaily.util.Converters
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.common.reflect.TypeToken
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


@HiltViewModel
class SharedSignInViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val authManager: AuthenticationManager,
    private val userProfileManager: UserProfileManager,
    private val userConsentManager: UserConsentManager,
    private val urlManager: UrlManager,
    private val remoteUserRepository: RemoteUserRepository,
    private val toastManager: ToastManager,
    private val userProfileMapper: UserProfileMapper,
    private val stringProvider: StringProvider
) : ViewModel() {

    private companion object {
        const val TAG = "SharedSignInViewModel"
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState = _authState.asStateFlow()

    private val _userProfileJson = MutableStateFlow<String?>(null)
    val userProfileJson = _userProfileJson.asStateFlow()

    private val _userProfileJsonV2 = MutableStateFlow<String?>(null)
    val userProfileJsonV2 = _userProfileJsonV2.asStateFlow()

    val consent = userConsentManager.consent
    val isAgreeAllChecked = userConsentManager.isAgreeAllChecked

    /**
     * LoginScreen
     */

    fun signInWithGoogle() {
        viewModelScope.launch {
            _authState.emit(AuthState.Loading)
            safeAuthCall {
                val result = authManager.getGoogleCredential()
                processSignInResult(result)
            }
        }
    }

    private suspend fun processSignInResult(result: GetCredentialResponse) {
        val credential = result.credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                val idToken = authManager.extractIdToken(credential)
                val authResult = authManager.authenticateWithFirebase(idToken)
                handleAuthResult(authResult)
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _authState.value = AuthState.Error("Invalid credentials provided: ${e.message}")
            } catch (e: FirebaseAuthException) {
                _authState.value = AuthState.Error("Firebase authentication failed: ${e.message}")
            } catch (e: Exception) {
                _authState.value = AuthState.Error("An error occurred during sign-in: ${e.message}")
            }
        } else {
            _authState.value = AuthState.Error("Invalid credential type received.")
        }
    }

    private suspend fun handleAuthResult(authResult: AuthResult) {
        try {
            val firebaseUser = authResult.user
                ?: throw IllegalStateException("Firebase user is null")
            val userId = firebaseUser.uid.takeIf { it.isNotBlank() }
                ?: throw IllegalStateException("Firebase user ID is blank")

            val userProfileMap = userProfileManager.createInitialProfile(firebaseUser, userId)
            val userProfileJson = userProfileMapper.convertMapToJson(userProfileMap)
            _userProfileJson.value = userProfileJson

            if (remoteUserRepository.isUserRegistered(userId)) {
                remoteUserRepository.updateLastSyncDate(userId)
                remoteUserRepository.syncFirestoreUserToRoom(userId)
                _authState.value = AuthState.Authenticated(userId)
            } else {
                _authState.value = AuthState.RequiresConsent(userId, userProfileJson)
            }
        } catch (e: IllegalArgumentException) {
            Log.e("handleAuthResult", "Invalid input data", e)
            _authState.value = AuthState.Error("Invalid input: ${e.message}")
        } catch (e: FirebaseFirestoreException) {
            Log.e("handleAuthResult", "Firestore operation failed", e)
            _authState.value = AuthState.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            Log.e("handleAuthResult", "Authentication failed", e)
            _authState.value = AuthState.Error("Authentication failed: ${e.message}")
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

        val updatedJson = userProfileManager.updateUserProfileWithConsent(
            userProfileJson,
            consent.value
        )
        _userProfileJsonV2.value = updatedJson

        updatedJson?.let { json ->
            viewModelScope.launch {
                try {
                    saveUserProfile(json)
                    showSignUpSuccessMessage()
                } catch (e: FirebaseFirestoreException) {
                    AuthState.Error("Network error while saving profile")
                        .also { _authState.value = it }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to save user profile", e)
                    showSignUpErrorMessage()
                }
            }
        }
    }

    private suspend fun saveUserProfile(json: String) {
        userProfileManager.saveUserToRoom(json)
        userProfileManager.saveUserToFirestore(json, auth.currentUser)
    }

    fun openUrl(context: Context, url: String) {
        urlManager.openUrl(context, url)
    }

    private suspend fun safeAuthCall(block: suspend () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is GetCredentialException ->
                    stringProvider.getString(R.string.error_credential_retrieval)
                is NetworkOnMainThreadException ->
                    stringProvider.getString(R.string.error_network_main_thread)
                is FirebaseAuthInvalidCredentialsException ->
                    stringProvider.getString(
                        R.string.error_invalid_credentials,
                        e.message.orEmpty()
                    )
                is FirebaseAuthException ->
                    stringProvider.getString(
                        R.string.error_firebase_auth,
                        e.message.orEmpty()
                    )
                is FirebaseFirestoreException ->
                    stringProvider.getString(R.string.error_network)
                else -> stringProvider.getString(
                    R.string.error_unexpected,
                    e.message.orEmpty()
                )
            }
            Log.e(TAG, "Error in auth operation", e)
            _authState.emit(AuthState.Error(errorMessage))
        }
    }

    private fun showSignUpSuccessMessage() {
        toastManager.showToast("환영합니다! 회원가입이 완료되었습니다.")
    }

    private fun showSignUpErrorMessage() {
        toastManager.showToast("회원가입 중 오류가 발생했습니다. 다시 시도해주세요.")
    }
}
