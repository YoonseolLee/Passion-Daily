package com.example.passionDaily.manager

import android.util.Log
import com.example.passionDaily.data.repository.local.LocalUserRepository
import com.example.passionDaily.data.repository.remote.RemoteUserRepository
import com.example.passionDaily.mapper.UserProfileMapper
import com.example.passionDaily.util.TimeUtil
import com.example.passionDaily.util.UserConsent
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject

class UserProfileManager @Inject constructor(
    private val userProfileMapper: UserProfileMapper,
    private val remoteUserRepository: RemoteUserRepository,
    private val localUserRepository: LocalUserRepository
) {
    private val _isJsonValid = MutableStateFlow(false)
    val isJsonValid = _isJsonValid.asStateFlow()

    private companion object {
        const val TAG = "UserProfileManager"
    }

    fun createInitialProfile(firebaseUser: FirebaseUser, userId: String): Map<String, Any?> {
        try {
            require(!userId.isBlank()) { "User ID cannot be blank" }
            require(firebaseUser.uid.isNotBlank()) { "Firebase user ID cannot be blank" }

            val now = TimeUtil.getCurrentTimestamp()
            val profileMap = buildUserProfileMap(firebaseUser, userId, now)

            return profileMap
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Invalid input parameters", e)
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create initial profile", e)
            throw IllegalStateException("Failed to create initial profile", e)
        }
    }

    private fun buildUserProfileMap(
        firebaseUser: FirebaseUser,
        userId: String,
        timestamp: String
    ): Map<String, Any?> {
        try {
            if (timestamp.isBlank()) {
                throw IllegalArgumentException("Timestamp cannot be blank")
            }

            if (firebaseUser.email.isNullOrBlank()) {
                Log.w(TAG, "Firebase user email is null or blank")
            }

            return mapOf(
                "id" to userId,
                "email" to firebaseUser.email,
                "role" to "USER",
                "lastLoginDate" to timestamp,
                "fcmToken" to null,
                "notificationEnabled" to true,
                "notificationTime" to "12:00",
                "privacyPolicyEnabled" to null,
                "termsOfServiceEnabled" to null,
                "lastSyncDate" to timestamp,
                "isAccountDeleted" to false,
                "createdDate" to timestamp,
                "modifiedDate" to timestamp
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to build user profile map", e)
            throw IllegalStateException("Failed to build user profile map", e)
        }
    }

    fun verifyJson(json: String?): Boolean {
        if (json.isNullOrEmpty()) {
            Log.e("UserProfileJsonManager", "Invalid JSON: null or empty")
            _isJsonValid.value = false
            return false
        }

        return try {
            JSONObject(json)
            Log.d("UserProfileJsonManager", "Valid JSON: $json")
            _isJsonValid.value = true
            true
        } catch (e: JSONException) {
            Log.e("UserProfileJsonManager", "Invalid JSON format: $json", e)
            _isJsonValid.value = false
            false
        }
    }

    fun updateUserProfileWithConsent(
        userProfileJson: String?,
        consent: UserConsent
    ): String? {
        if (!_isJsonValid.value || userProfileJson == null) {
            Log.e(TAG, "Cannot update invalid JSON")
            return null
        }
        return try {
            val jsonObject = JSONObject(userProfileJson)
            jsonObject.apply {
                put("privacyPolicyEnabled", consent.privacyPolicy)
                put("termsOfServiceEnabled", consent.termsOfService)
            }.toString()
        } catch (e: JSONException) {
            Log.e(TAG, "Failed to update user profile with consent", e)
            null
        }
    }

    private fun extractUserInfo(userProfileJson: String): Pair<Map<String, Any?>, String> {
        try {
            val profileMap = userProfileMapper.mapFromJson(userProfileJson)
            val userId = profileMap["id"] as String?
                ?: throw IllegalStateException("User ID not found in profile")
            return Pair(profileMap, userId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract user info from JSON", e)
            throw IllegalArgumentException("Invalid user profile format", e)
        }
    }

    suspend fun saveUserToFirestore(userProfileJson: String, firebaseUser: FirebaseUser?): String {
        if (firebaseUser == null) {
            Log.e(TAG, "Firebase user is null")
            throw IllegalStateException("Firebase user must not be null")
        }

        try {
            val (profileMap, userId) = extractUserInfo(userProfileJson)
            remoteUserRepository.addUserProfile(userId, profileMap)
            return userId
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save user to Firestore", e)
            throw e
        }
    }

    suspend fun saveUserToRoom(userProfileJson: String): String {
        try {
            val (profileMap, userId) = extractUserInfo(userProfileJson)
            val userEntity = userProfileMapper.mapToUserEntity(profileMap)
            localUserRepository.saveUser(userEntity)
            return userId
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save user to Room", e)
            throw e
        }
    }
}
