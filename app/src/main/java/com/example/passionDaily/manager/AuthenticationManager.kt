package com.example.passionDaily.manager

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.example.passionDaily.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
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
}