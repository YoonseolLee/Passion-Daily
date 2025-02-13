package com.example.passionDaily.login.manager

import com.example.passionDaily.login.domain.model.UserConsent
import com.google.firebase.auth.FirebaseUser

interface UserProfileManager {
    fun createInitialProfile(firebaseUser: FirebaseUser, userId: String): Map<String, Any?>
    suspend fun verifyJson(json: String?): Boolean
    suspend fun updateUserProfileWithConsent(userProfileJson: String, consent: UserConsent): String?
    suspend fun saveUserToFirestore(userProfileJson: String): String
    suspend fun extractUserInfo(userProfileJson: String): Pair<Map<String, Any?>, String>
    suspend fun saveUserToRoom(userProfileJson: String): String
    suspend fun syncExistingUser(userId: String)
    suspend fun setAuthenticated(userId: String)
}