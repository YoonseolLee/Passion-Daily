package com.example.passionDaily.login.manager

import com.example.passionDaily.login.domain.model.UserConsent
import com.example.passionDaily.login.domain.usecase.CreateInitialProfileUseCase
import com.example.passionDaily.login.domain.usecase.ManageJsonUseCase
import com.example.passionDaily.login.domain.usecase.SaveUserProfileUseCase
import com.example.passionDaily.login.stateholder.UserProfileStateHolder
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject

class UserProfileManager @Inject constructor(
    private val userProfileStateHolder: UserProfileStateHolder,
    private val createInitialProfileUseCase: CreateInitialProfileUseCase,
    private val saveUserProfileUseCase: SaveUserProfileUseCase,
    private val manageJsonUseCase: ManageJsonUseCase
) {
    private val isJsonValid = userProfileStateHolder.isJsonValid

    fun createInitialProfile(firebaseUser: FirebaseUser, userId: String): Map<String, Any?> {
        return createInitialProfileUseCase.createInitialProfile(firebaseUser, userId)
    }

    suspend fun verifyJson(json: String?): Boolean {
        return manageJsonUseCase.verifyJson(json)
    }

    suspend fun updateUserProfileWithConsent(
        userProfileJson: String,
        consent: UserConsent
    ): String? {
        return manageJsonUseCase.updateUserProfileWithConsent(userProfileJson, consent)
    }

    suspend fun saveUserToFirestore(userProfileJson: String): String {
        val (profileMap, userId) = extractUserInfo(userProfileJson)
        return saveUserProfileUseCase.saveUserToFirestore(userId, profileMap)
    }

    suspend fun extractUserInfo(userProfileJson: String): Pair<Map<String, Any?>, String> {
        return manageJsonUseCase.extractUserInfo(userProfileJson)
    }

    suspend fun saveUserToRoom(userProfileJson: String): String {
        val (profileMap, userId) = extractUserInfo(userProfileJson)
        return saveUserProfileUseCase.saveToRoom(profileMap, userId)
    }

    suspend fun syncExistingUser(userId: String) {
        saveUserProfileUseCase.syncExistingUser(userId)
    }
}
