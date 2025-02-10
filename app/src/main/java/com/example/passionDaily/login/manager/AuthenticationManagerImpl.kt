package com.example.passionDaily.login.manager

import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialResponse
import com.example.passionDaily.login.domain.usecase.GetFirebaseUserUseCase
import com.example.passionDaily.login.domain.usecase.GetGoogleCredentialUseCase
import com.example.passionDaily.login.stateholder.LoginStateHolder
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject

class AuthenticationManagerImpl @Inject constructor(
    private val getGoogleCredentialUseCase: GetGoogleCredentialUseCase,
    private val getFirebaseUserUseCase: GetFirebaseUserUseCase,
    private val loginStateHolder: LoginStateHolder,
) : AuthenticationManager {
    override suspend fun getGoogleCredential(): GetCredentialResponse {
        return getGoogleCredentialUseCase.getGoogleCredential()
    }

    override suspend fun authenticateWithFirebase(idToken: String): AuthResult {
        return getGoogleCredentialUseCase.authenticateWithFirebase(idToken)
    }

    override suspend fun extractIdToken(credential: CustomCredential): String {
        return getGoogleCredentialUseCase.extractIdToken(credential)
    }

    override fun getFirebaseUser(authResult: AuthResult): FirebaseUser {
        return getFirebaseUserUseCase.getFirebaseUser(authResult)
    }

    override fun getUserId(firebaseUser: FirebaseUser): String {
        return getFirebaseUserUseCase.getUserId(firebaseUser)
    }

    override suspend fun clearCredentials() {
        getGoogleCredentialUseCase.clearCredentials()
    }

    override suspend fun startLoading() {
        loginStateHolder.updateIsLoading(true)
    }

    override suspend fun stopLoading() {
        loginStateHolder.updateIsLoading(false)
    }

    override suspend fun updateUserProfileJson(json: String?) {
        loginStateHolder.updateUserProfileJson(json)
    }

    override suspend fun updateUserProfileJsonV2(json: String?) {
        loginStateHolder.updateUserProfileJsonV2(json)
    }

    override suspend fun updateIsLoggedIn(isLoggedIn: Boolean) {
        loginStateHolder.updateIsLoggedIn(isLoggedIn)
    }
}