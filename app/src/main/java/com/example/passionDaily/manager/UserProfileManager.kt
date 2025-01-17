package com.example.passionDaily.manager

import com.example.passionDaily.util.TimeUtil
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject

class UserProfileManager @Inject constructor() {
    fun createInitialProfile(firebaseUser: FirebaseUser, userId: String): Map<String, Any?> {
        val now = TimeUtil.getCurrentTimestamp()
        return buildUserProfileMap(firebaseUser, userId, now)
    }

    private fun buildUserProfileMap(
        firebaseUser: FirebaseUser,
        userId: String,
        timestamp: String
    ): Map<String, Any?> {
        return mapOf(
            "id" to userId,
            "email" to firebaseUser.email,
            "role" to "USER",
            "lastLoginDate" to timestamp,
            "notificationEnabled" to true,
            "notificationTime" to "08:00",
            "privacyPolicyEnabled" to null,
            "termsOfServiceEnabled" to null,
            "lastSyncDate" to timestamp,
            "isAccountDeleted" to false,
            "createdDate" to timestamp,
            "modifiedDate" to timestamp
        )
    }
}
