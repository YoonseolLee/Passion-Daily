package com.example.passionDaily.login.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passionDaily.login.base.SharedLogInActions
import com.example.passionDaily.login.base.SharedLogInState
import com.example.passionDaily.login.manager.AuthenticationManager
import com.example.passionDaily.login.manager.UrlManager
import com.example.passionDaily.login.manager.UserConsentManager
import com.example.passionDaily.login.manager.UserProfileManager
import com.example.passionDaily.user.data.remote.repository.RemoteUserRepository
import com.example.passionDaily.toast.manager.ToastManager
import com.example.passionDaily.util.mapper.UserProfileMapper
import com.example.passionDaily.resources.StringProvider
import com.example.passionDaily.login.stateholder.AuthStateHolder
import com.example.passionDaily.login.stateholder.LoginStateHolder
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
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
) : ViewModel(), SharedLogInActions, SharedLogInState {
    override val authState = authStateHolder.authState

    override val userProfileJson = loginStateHolder.userProfileJson
    override val userProfileJsonV2 = loginStateHolder.userProfileJsonV2
    override val isLoading = loginStateHolder.isLoading

    override val consent = userConsentManager.consent
    override val isAgreeAllChecked = userConsentManager.isAgreeAllChecked

    /**
     * LoginScreen
     */

    override fun signInWithGoogle() {
        viewModelScope.launch {
            Log.d("SignInVM", "Starting Google Sign-in process")
            authManager.startLoading()
            try {
                Log.d("SignInVM", "Clearing existing credentials")
                authManager.clearCredentials()

                Log.d("SignInVM", "Requesting new Google credential")
                val result = authManager.getGoogleCredential()
                processSignInResult(result)
            } catch (e: Exception) {
                Log.e("SignInVM", "Error in signInWithGoogle: ${e.message}", e)
                handleException(e)
            } finally {
                Log.d("SignInVM", "Sign-in process completed, stopping loading")
                authManager.stopLoading()
            }
        }
    }

    private suspend fun processSignInResult(result: GetCredentialResponse) {
        Log.d("SignInVM", "Processing sign-in result")
        val credential = result.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            try {
                Log.d("SignInVM", "Extracting ID token from credential")
                val idToken = authManager.extractIdToken(credential)
                Log.d("SignInVM", "Authenticating with Firebase")
                val authResult = authManager.authenticateWithFirebase(idToken)
                handleAuthResult(authResult)
            } catch (e: Exception) {
                Log.e("SignInVM", "Error in processSignInResult: ${e.message}", e)
                handleException(e)
            }
        } else {
            Log.e("SignInVM", "Invalid credential type: ${credential?.type}")
        }
    }

    private suspend fun handleAuthResult(authResult: AuthResult) {
        Log.d("SignInVM", "Handling auth result")
        try {
            Log.d("SignInVM", "Getting Firebase user")
            val firebaseUser = authManager.getFirebaseUser(authResult)
            Log.d("SignInVM", "Getting user ID")
            val userId = authManager.getUserId(firebaseUser)
            Log.d("SignInVM", "Creating initial profile for user: $userId")
            val userProfileMap = userProfileManager.createInitialProfile(firebaseUser, userId)

            Log.d("SignInVM", "Storing user profile")
            val userProfileJson = storeUserProfile(userProfileMap)
            handleUserRegistrationStatus(userId, userProfileJson)
        } catch (e: Exception) {
            Log.e("SignInVM", "Error in handleAuthResult: ${e.message}", e)
            handleException(e)
        }
    }

    private suspend fun storeUserProfile(userProfileMap: Map<String, Any?>): String {
        Log.d("SignInVM", "Converting profile map to JSON")
        val userProfileJson = userProfileMapper.convertMapToJson(userProfileMap)
        Log.d("SignInVM", "Updating user profile JSON")
        authManager.updateUserProfileJson(userProfileJson)
        return userProfileJson
    }

    private suspend fun handleUserRegistrationStatus(userId: String, userProfileJson: String) {
        Log.d("SignInVM", "Checking user registration status for user: $userId")
        if (remoteUserRepository.isUserRegistered(userId)) {
            Log.d("SignInVM", "User is already registered, syncing existing user")
            syncExistingUser(userId)
        } else {
            Log.d("SignInVM", "New user detected, setting requires consent")
            authStateHolder.setRequiresConsent(userId, userProfileJson)
        }
    }

    private suspend fun syncExistingUser(userId: String) {
        try {
            Log.d("SignInVM", "Starting sync for existing user: $userId")
            userProfileManager.syncExistingUser(userId)
            Log.d("SignInVM", "User sync completed successfully")
        } catch (e: Exception) {
            Log.e("SignInVM", "Error syncing existing user: ${e.message}", e)
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
    override fun toggleAgreeAll() {
        userConsentManager.toggleAgreeAll()
    }

    // 개별 항목 토글 메서드
    override fun toggleIndividualItem(item: String) {
        userConsentManager.toggleIndividualItem(item)
    }

    override fun handleNextClick(userProfileJson: String?) {
        Log.d("ConsentVM", "Starting handleNextClick, consent all agreed: ${consent.value.isAllAgreed}")
        if (!consent.value.isAllAgreed) {
            Log.d("ConsentVM", "Consent not all agreed, returning")
            return
        }

        viewModelScope.launch {
            try {
                if (userProfileJson != null) {
                    Log.d("ConsentVM", "Updating user profile with consent")
                    val updatedJson = userProfileManager.updateUserProfileWithConsent(
                        userProfileJson,
                        consent.value
                    )

                    if (updatedJson != null) {
                        Log.d("ConsentVM", "Profile updated successfully, saving to storage")
                        authManager.updateUserProfileJsonV2(updatedJson)
                        saveUserProfile(updatedJson)

                        val currentUser = FirebaseAuth.getInstance().currentUser
                        Log.d("ConsentVM", "Current Firebase user: ${currentUser?.uid}")
                        val userId = currentUser?.uid ?: run {
                            Log.e("ConsentVM", "Firebase user is null")
                            toastManager.showLoginErrorToast()
                            return@launch
                        }
                        setAuthenticated(userId)
                        toastManager.showLoginSuccessToast()
                        Log.d("ConsentVM", "User authentication completed successfully")
                    } else {
                        Log.e("ConsentVM", "Failed to update user profile")
                        toastManager.showLoginErrorToast()
                    }
                } else {
                    Log.w("ConsentVM", "userProfileJson is null, showing success toast anyway")
                    toastManager.showLoginSuccessToast()
                }
            } catch (e: Exception) {
                Log.e("ConsentVM", "Error in handleNextClick: ${e.message}", e)
                handleException(e)
            }
        }
    }

    private suspend fun saveUserProfile(json: String) {
        try {
            Log.d("ConsentVM", "Saving user profile to Room")
            userProfileManager.saveUserToRoom(json)
            Log.d("ConsentVM", "Saving user profile to Firestore")
            userProfileManager.saveUserToFirestore(json)
            Log.d("ConsentVM", "User profile saved successfully")
        } catch (e: Exception) {
            Log.e("ConsentVM", "Error saving user profile: ${e.message}", e)
            handleException(e)
        }
    }

    private suspend fun setAuthenticated(userId: String) {
        try {
            Log.d("ConsentVM", "Setting user as authenticated: $userId")
            userProfileManager.setAuthenticated(userId)
            Log.d("ConsentVM", "User authenticated successfully")
        } catch (e: Exception) {
            Log.e("ConsentVM", "Error setting user as authenticated: ${e.message}", e)
            handleException(e)
        }
    }

    override fun openUrl(context: Context, url: String) {
        urlManager.openUrl(context, url)
    }

    /**
     * QuoteScreen
     */

    override fun signalLoginSuccess() {
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

    override fun handleException(e: Exception) {
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
