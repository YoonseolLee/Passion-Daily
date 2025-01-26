package com.example.passionDaily.ui.viewmodels

import android.content.Context
import android.os.NetworkOnMainThreadException
import android.util.Log
import android.widget.Toast
import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.R
import com.example.passionDaily.data.repository.remote.RemoteUserRepository
import com.example.passionDaily.manager.AuthenticationManager
import com.example.passionDaily.manager.ToastManager
import com.example.passionDaily.manager.UrlManager
import com.example.passionDaily.manager.UserConsentManager
import com.example.passionDaily.manager.UserProfileManager
import com.example.passionDaily.mapper.UserProfileMapper
import com.example.passionDaily.resources.StringProvider
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
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

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    private val _navigationEvents = Channel<NavigationEvent>()
    val navigationEvents = _navigationEvents.receiveAsFlow()

    sealed class NavigationEvent {
        object NavigateToQuote : NavigationEvent()
        data class NavigateToTermsConsent(val userProfileJson: String) : NavigationEvent()
    }

    val consent = userConsentManager.consent
    val isAgreeAllChecked = userConsentManager.isAgreeAllChecked

    /**
     * LoginScreen
     */

    fun signInWithGoogle() {
        viewModelScope.launch {
            safeAuthCall {
                val result = authManager.getGoogleCredential()
                processSignInResult(result)
            }
        }
    }

    private suspend fun processSignInResult(result: GetCredentialResponse) {
        val credential = result.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            safeAuthCall {
                val idToken = authManager.extractIdToken(credential)
                val authResult = authManager.authenticateWithFirebase(idToken)
                handleAuthResult(authResult)
            }
        } else {
            _authState.emit(AuthState.Error(stringProvider.getString(R.string.error_invalid_credential)))
        }
    }

    private suspend fun handleAuthResult(authResult: AuthResult) {
        safeAuthCall {
            val firebaseUser =
                requireNotNull(authResult.user) { stringProvider.getString(R.string.error_firebase_user_null) }
            val userId =
                requireNotNull(firebaseUser.uid.takeIf { it.isNotBlank() }) {
                    stringProvider.getString(
                        R.string.error_firebase_user_id_blank
                    )
                }
            val userProfileMap = createUserProfile(firebaseUser, userId)
            storeUserProfile(userProfileMap)
            handleUserRegistrationStatus(userId)
        }
    }

    private fun createUserProfile(firebaseUser: FirebaseUser, userId: String): Map<String, Any?> {
        val userProfileMap = userProfileManager.createInitialProfile(firebaseUser, userId)
        return userProfileMap
    }

    private suspend fun storeUserProfile(userProfileMap: Map<String, Any?>) {
        val userProfileJson = userProfileMapper.convertMapToJson(userProfileMap)
        _userProfileJson.emit(userProfileJson)
    }

    private suspend fun handleUserRegistrationStatus(userId: String) {
        if (remoteUserRepository.isUserRegistered(userId)) {
            syncExistingUser(userId)
        } else {
            _authState.emit(AuthState.RequiresConsent(userId, userProfileJson.value))
        }
    }

    private suspend fun syncExistingUser(userId: String) {
        remoteUserRepository.updateLastSyncDate(userId)
        remoteUserRepository.syncFirestoreUserToRoom(userId)
        _authState.emit(AuthState.Authenticated(userId))
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
            safeAuthCall {
                val updatedJson = userProfileManager.updateUserProfileWithConsent(
                    userProfileJson,
                    consent.value
                )
                _userProfileJsonV2.emit(updatedJson)

                when {
                    updatedJson != null -> {
                        saveUserProfile(updatedJson)
                        showSignUpSuccessMessage()
                    }

                    else -> showSignUpErrorMessage()
                }
            }
        }
    }

    private suspend fun saveUserProfile(json: String) {
        userProfileManager.saveUserToRoom(json)
        userProfileManager.saveUserToFirestore(json, auth.currentUser)
    }

    private fun showSignUpSuccessMessage() {
        val signUpSuccessMessage = stringProvider.getString(R.string.signup_success)
        toastManager.showToast(signUpSuccessMessage)
    }

    private fun showSignUpErrorMessage() {
        val signUpErrorMessage = stringProvider.getString(R.string.error_database)
        toastManager.showToast(signUpErrorMessage)
    }

    fun openUrl(context: Context, url: String) {
        urlManager.openUrl(context, url)
    }

    fun signalLoginSuccess() {
        viewModelScope.launch {
            _isLoggedIn.emit(true)
            delay(100)
            showLoginSuccessMessage()
            _navigationEvents.send(NavigationEvent.NavigateToQuote)
        }
    }

    fun signalLoginError(errorMessage: String) {
        viewModelScope.launch {
            _isLoggedIn.emit(false)
            showLoginErrorMessage(errorMessage)
        }
    }

    private fun showLoginSuccessMessage() {
        val message = stringProvider.getString(R.string.login_success)
        toastManager.showToast(message)
    }

    private fun showLoginErrorMessage(errorMessage: String) {
        val message = stringProvider.getString(
            R.string.login_error_format,
            errorMessage
        )
        toastManager.showToast(message, Toast.LENGTH_LONG)
    }

    private fun showLoadingMessage() {
        val message = stringProvider.getString(R.string.login_loading)
        toastManager.showToast(message)
    }

    fun showUnauthenticatedMessage() {
        val message = stringProvider.getString(R.string.login_required)
        toastManager.showToast(message)
    }

    private suspend fun safeAuthCall(block: suspend () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            val errorMessage = mapExceptionToErrorMessage(e)
            Log.e(TAG, "Error in auth operation", e)
            _authState.emit(AuthState.Error(errorMessage))
        }
    }

    private fun mapExceptionToErrorMessage(e: Exception): String {
        return when (e) {
            is GetCredentialException ->
                stringProvider.getString(R.string.error_credential_retrieval)
            is NetworkOnMainThreadException ->
                stringProvider.getString(R.string.error_network_main_thread)
            is FirebaseAuthInvalidCredentialsException ->
                stringProvider.getString(R.string.error_invalid_credential, e.message.orEmpty())
            is FirebaseAuthException ->
                stringProvider.getString(R.string.error_firebase_auth, e.message.orEmpty())
            is FirebaseFirestoreException ->
                stringProvider.getString(R.string.error_network)
            else ->
                stringProvider.getString(R.string.error_unexpected, e.message.orEmpty())
        }
    }
}
