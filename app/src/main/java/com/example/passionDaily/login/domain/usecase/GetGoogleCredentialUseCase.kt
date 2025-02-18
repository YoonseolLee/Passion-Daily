package com.example.passionDaily.login.domain.usecase

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.passionDaily.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetGoogleCredentialUseCase @Inject constructor(
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) {
    suspend fun getGoogleCredential(): GetCredentialResponse = withContext(Dispatchers.IO) {
        val credentialManager = createCredentialManager()
        val request = createCredentialRequest()
        fetchCredential(credentialManager, request)
    }

    private fun createCredentialManager(): CredentialManager {
        return CredentialManager.create(context)
    }

    private fun createCredentialRequest(): GetCredentialRequest {
        val clientId = context.getString(R.string.client_id)
        val googleIdOption = GetSignInWithGoogleOption.Builder(clientId)
            .build()
        return GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }


    private suspend fun fetchCredential(
        credentialManager: CredentialManager,
        request: GetCredentialRequest
    ): GetCredentialResponse {
        return credentialManager.getCredential(context, request)
    }

    suspend fun authenticateWithFirebase(idToken: String): AuthResult =
        withContext(Dispatchers.IO) {
            val firebaseCredential = createFirebaseCredential(idToken)
            signInWithFirebaseCredential(firebaseCredential)
        }

    private fun createFirebaseCredential(idToken: String): AuthCredential {
        return GoogleAuthProvider.getCredential(idToken, null)
    }

    private suspend fun signInWithFirebaseCredential(firebaseCredential: AuthCredential): AuthResult {
        return auth.signInWithCredential(firebaseCredential).await()
    }

    suspend fun extractIdToken(credential: CustomCredential): String = withContext(Dispatchers.IO) {
        getIdTokenFromCredential(credential)
    }

    private fun getIdTokenFromCredential(credential: CustomCredential): String {
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        return googleIdTokenCredential.idToken
    }

    suspend fun clearCredentials() = withContext(Dispatchers.IO) {
        try {
            clearCredentialState()

            suspendCancellableCoroutine { continuation ->
                // 리스너를 변수로 선언
                val listener = object : FirebaseAuth.AuthStateListener {
                    override fun onAuthStateChanged(auth: FirebaseAuth) {
                        if (auth.currentUser == null) {
                            // 로그아웃이 완료되면 리스너 제거 후 코루틴 재개
                            auth.removeAuthStateListener(this)
                            if (continuation.isActive) {
                                continuation.resume(Unit, null)
                            }
                        }
                    }
                }

                // 리스너 등록 후 signOut 실행
                auth.addAuthStateListener(listener)
                auth.signOut()

                // 코루틴이 취소되면 리스너 제거
                continuation.invokeOnCancellation {
                    auth.removeAuthStateListener(listener)
                }
            }

            Log.d("clearCredentials", "Auth credentials cleared successfully")
        } catch (e: Exception) {
            handleClearCredentialsError(e)
        }
    }

    private suspend fun clearCredentialState() {
        val credentialManager = createCredentialManager()
        val request = ClearCredentialStateRequest()
        credentialManager.clearCredentialState(request)
        Log.d("clearCredentials", "Google credential cleared")
    }

    private fun signOutFirebase() {
        auth.signOut()
        Log.d("clearCredentials", "Auth credentials cleared successfully")
    }

    private fun handleClearCredentialsError(e: Exception) {
        Log.e("AuthenticationManager", "Error clearing credentials", e)
        throw e
    }
}
