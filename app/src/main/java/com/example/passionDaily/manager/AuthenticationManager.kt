package com.example.passionDaily.manager

import android.content.Context
import android.util.Log
import android.webkit.CookieManager
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.example.passionDaily.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthenticationManager @Inject constructor(
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) {
    suspend fun getGoogleCredential(): GetCredentialResponse {
        val credentialManager = CredentialManager.create(context)
        val clientId = context.getString(R.string.client_id)

        val googleIdOption = GetSignInWithGoogleOption.Builder(clientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return credentialManager.getCredential(context, request)
    }

    suspend fun authenticateWithFirebase(idToken: String): AuthResult {
        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
        return auth.signInWithCredential(firebaseCredential).await()
    }

    fun extractIdToken(credential: CustomCredential): String {
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        return googleIdTokenCredential.idToken
    }

    suspend fun clearCredentials() = withContext(Dispatchers.IO) {
        try {
            val credentialManager = CredentialManager.create(context)
            val request = ClearCredentialStateRequest()

            credentialManager.clearCredentialState(request)
            Log.d("clearCredentials", "Google credential cleared")

            auth.signOut()

            Log.d("clearCredentials", "Auth credentials cleared successfully")
        } catch (e: Exception) {
            Log.e("AuthenticationManager", "Error clearing credentials", e)
            throw e
        }
    }

    fun getCurrentUser() = Firebase.auth.currentUser

    fun signOut() {
        Firebase.auth.signOut()
    }

    suspend fun deleteAccount(user: FirebaseUser) {
        user.delete().await()
    }
}