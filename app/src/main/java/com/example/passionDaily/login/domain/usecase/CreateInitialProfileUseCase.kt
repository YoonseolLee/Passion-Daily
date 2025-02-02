package com.example.passionDaily.login.domain.usecase

import com.example.passionDaily.constants.UseCaseConstants.UserProfileConstants
import com.example.passionDaily.login.UserProfileKey
import com.example.passionDaily.util.TimeUtil
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject

class CreateInitialProfileUseCase @Inject constructor() {
    fun createInitialProfile(firebaseUser: FirebaseUser, userId: String): Map<String, Any?> {
        validateUserData(firebaseUser, userId)
        val now = TimeUtil.getCurrentTimestamp()
        return buildUserProfileMap(firebaseUser, userId, now)
    }

    /**
     * 검증 실패 시, IllegalArgumentException을 throw한다.
     */
    private fun validateUserData(firebaseUser: FirebaseUser, userId: String) {
        require(userId.isNotBlank()) { "User ID cannot be blank" }
        require(firebaseUser.uid.isNotBlank()) { "Firebase user ID cannot be blank" }
        require(firebaseUser.email?.isNotBlank() == true) { "Firebase user email cannot be empty" }
        require(firebaseUser.uid == userId) { "Firebase UID does not match with user ID" }
    }

    private fun buildUserProfileMap(
        firebaseUser: FirebaseUser,
        userId: String,
        timestamp: String
    ): Map<String, Any?> {
        return mapOf(
            UserProfileKey.ID.key to userId,
            UserProfileKey.EMAIL.key to firebaseUser.email,
            UserProfileKey.ROLE.key to UserProfileConstants.ROLE_USER,
            UserProfileKey.LAST_LOGIN_DATE.key to timestamp,
            UserProfileKey.FCM_TOKEN.key to null,
            UserProfileKey.NOTIFICATION_ENABLED.key to UserProfileConstants.DEFAULT_NOTIFICATION_ENABLED,
            UserProfileKey.NOTIFICATION_TIME.key to UserProfileConstants.DEFAULT_NOTIFICATION_TIME,
            UserProfileKey.PRIVACY_POLICY_ENABLED.key to null,
            UserProfileKey.TERMS_OF_SERVICE_ENABLED.key to null,
            UserProfileKey.LAST_SYNC_DATE.key to timestamp,
            UserProfileKey.IS_ACCOUNT_DELETED.key to false,
            UserProfileKey.CREATED_DATE.key to timestamp,
            UserProfileKey.MODIFIED_DATE.key to timestamp
        )
    }
}