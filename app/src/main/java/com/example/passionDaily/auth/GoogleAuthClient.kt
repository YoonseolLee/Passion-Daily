package com.example.passionDaily.auth

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.example.passionDaily.data.remote.model.GoogleAuthUser
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class GoogleAuthClient @Inject constructor(
    private val context: Context
) {

    private val firebaseAuth: FirebaseAuth = Firebase.auth
    private val credentialManager = CredentialManager.create(context)
    private val clientId =
        "CLIENT_ID_추가해야함"

    // currentUser가 true이면, isSignedIn()이 true이다.
    fun isSignedIn(): Boolean = firebaseAuth.currentUser != null

    fun getSignedInUser(): GoogleAuthUser? = firebaseAuth.currentUser?.let { user ->
        GoogleAuthUser(
            userId = user.uid,
            username = user.displayName,
            email = user.email,
            profilePictureUrl = user.photoUrl?.toString()
        )
    }

    suspend fun signIn(): SignInResult {
        // 이미 로그인되어 있는 경우 바로 반환
        if (isSignedIn() == true) {
            return SignInResult.Success(getSignedInUser())
        }

        // 로그인되지 않은 경우 구글 로그인 시도
        try {
            val credentialResponse = requestGoogleCredential()
            val isSignInSuccessful = processGoogleCredential(credentialResponse)

            if (isSignInSuccessful) {
                return SignInResult.Success(getSignedInUser())
            }

            // 로그인 실패 시
            return SignInResult.Error("Sign in failed")

        } catch (e: Exception) {
            if (e is CancellationException) throw e
            return SignInResult.Error(e.message ?: "Unknown error occurred")
        }
    }


    private suspend fun requestGoogleCredential(): GetCredentialResponse {
        val credentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetSignInWithGoogleOption.Builder(clientId).build()
            )
            .build()

        return credentialManager.getCredential(context, credentialRequest)
    }

    private suspend fun processGoogleCredential(credentialResponse: GetCredentialResponse): Boolean {
        val credential = credentialResponse.credential

        return if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            try {
                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val authCredential =
                    GoogleAuthProvider.getCredential(googleCredential.idToken, null)
                val authResult = firebaseAuth.signInWithCredential(authCredential).await()
                authResult.user != null
            } catch (e: GoogleIdTokenParsingException) {
                logError("GoogleIdTokenParsingException: ${e.message}")
                false
            }
        } else {
            logError("Credential is not GoogleIdTokenCredential")
            false
        }
    }

    suspend fun signOut() {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
        firebaseAuth.signOut()
    }

    private fun logError(message: String) {
        println("GoogleAuthClient: $message")
    }
}

sealed class SignInResult {
    data class Success(val user: GoogleAuthUser?) : SignInResult()
    data class Error(val message: String) : SignInResult()
}