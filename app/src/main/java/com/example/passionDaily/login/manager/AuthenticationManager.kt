package com.example.passionDaily.login.manager

import androidx.credentials.CustomCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import androidx.credentials.GetCredentialResponse as GetCredentialResponse1

interface AuthenticationManager {
    suspend fun getGoogleCredential(): GetCredentialResponse1
    suspend fun authenticateWithFirebase(idToken: String): AuthResult
    suspend fun extractIdToken(credential: CustomCredential): String
    fun getFirebaseUser(authResult: AuthResult): FirebaseUser
    fun getUserId(firebaseUser: FirebaseUser): String
    suspend fun clearCredentials()
    suspend fun startLoading()
    suspend fun stopLoading()
    suspend fun updateUserProfileJson(json: String?)
    suspend fun updateUserProfileJsonV2(json: String?)
    suspend fun updateIsLoggedIn(isLoggedIn: Boolean)
}