package com.example.passionDaily.login.manager

import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialResponse
import com.example.passionDaily.R
import com.example.passionDaily.login.domain.usecase.GetFirebaseUserUseCase
import com.example.passionDaily.login.stateholder.LoginStateHolder
import com.example.passionDaily.login.domain.usecase.GetGoogleCredentialUseCase
import com.example.passionDaily.resources.StringProvider
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject

class AuthenticationManager @Inject constructor(
    private val getGoogleCredentialUseCase: GetGoogleCredentialUseCase,
    private val getFirebaseUserUseCase: GetFirebaseUserUseCase,
    private val loginStateHolder: LoginStateHolder,
    private val stringProvider: StringProvider
) {
    suspend fun getGoogleCredential(): GetCredentialResponse {
        return getGoogleCredentialUseCase.getGoogleCredential()
    }

    suspend fun authenticateWithFirebase(idToken: String): AuthResult {
        return getGoogleCredentialUseCase.authenticateWithFirebase(idToken)
    }

    suspend fun extractIdToken(credential: CustomCredential): String {
        return getGoogleCredentialUseCase.extractIdToken(credential)
    }

    fun getFirebaseUser(authResult: AuthResult): FirebaseUser {
        return getFirebaseUserUseCase.getFirebaseUser(authResult)
    }

    fun getUserId(firebaseUser: FirebaseUser): String {
        return getFirebaseUserUseCase.getUserId(firebaseUser)
    }

    suspend fun clearCredentials() {
        getGoogleCredentialUseCase.clearCredentials()
    }

    suspend fun startLoading() {
        loginStateHolder.updateIsLoading(true)
    }

    suspend fun stopLoading() {
        loginStateHolder.updateIsLoading(false)
    }

    suspend fun updateUserProfileJson(json: String?) {
        loginStateHolder.updateUserProfileJson(json)
    }

    suspend fun updateUserProfileJsonV2(json: String?) {
        loginStateHolder.updateUserProfileJsonV2(json)
    }

    suspend fun updateIsLoggedIn(isLoggedIn: Boolean) {
        loginStateHolder.updateIsLoggedIn(isLoggedIn)
    }
}