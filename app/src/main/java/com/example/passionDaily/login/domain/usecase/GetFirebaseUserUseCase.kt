package com.example.passionDaily.login.domain.usecase

import com.example.passionDaily.R
import com.example.passionDaily.resources.StringProvider
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject

class GetFirebaseUserUseCase @Inject constructor(
    private val stringProvider: StringProvider
) {
    fun getFirebaseUser(authResult: AuthResult): FirebaseUser {
        return requireNotNull(authResult.user) {
            stringProvider.getString(R.string.error_firebase_user_null)
        }
    }

    fun getUserId(firebaseUser: FirebaseUser): String {
        return requireNotNull(firebaseUser.uid.takeIf { it.isNotBlank() }) {
            stringProvider.getString(R.string.error_firebase_user_id_blank)
        }
    }
}