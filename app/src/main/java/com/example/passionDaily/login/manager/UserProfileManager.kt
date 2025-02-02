package com.example.passionDaily.login.manager

import android.database.sqlite.SQLiteException
import android.util.Log
import com.example.passionDaily.constants.ManagerConstants.UserProfile.TAG
import com.example.passionDaily.data.repository.local.LocalUserRepository
import com.example.passionDaily.data.repository.remote.RemoteUserRepository
import com.example.passionDaily.login.UserProfileKey
import com.example.passionDaily.mapper.UserProfileMapper
import com.example.passionDaily.util.TimeUtil
import com.example.passionDaily.login.domain.model.UserConsent
import com.example.passionDaily.login.domain.usecase.CreateInitialProfileUseCase
import com.example.passionDaily.login.domain.usecase.SaveUserProfileUseCase
import com.example.passionDaily.login.stateholder.UserProfileStateHolder
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject

class UserProfileManager @Inject constructor(
    private val userProfileStateHolder: UserProfileStateHolder,
    private val remoteUserRepository: RemoteUserRepository,
    private val createInitialProfileUseCase: CreateInitialProfileUseCase,
    private val saveUserProfileUseCase: SaveUserProfileUseCase
) {
    private val isJsonValid = userProfileStateHolder.isJsonValid

    fun createInitialProfile(firebaseUser: FirebaseUser, userId: String): Map<String, Any?> {
        return createInitialProfileUseCase.createInitialProfile(firebaseUser, userId)
    }

    fun verifyJson(json: String?): Boolean {
        if (json.isNullOrEmpty()) {
            Log.e("UserProfileJsonManager", "Invalid JSON: null or empty")
            userProfileStateHolder.updateIsJsonValid(false)
            return false
        }

        return try {
            JSONObject(json)
            Log.d("UserProfileJsonManager", "Valid JSON: $json")
            userProfileStateHolder.updateIsJsonValid(true)
            true
        } catch (e: JSONException) {
            Log.e("UserProfileJsonManager", "Invalid JSON format: $json", e)
            userProfileStateHolder.updateIsJsonValid(false)
            false
        }
    }

    fun updateUserProfileWithConsent(
        userProfileJson: String,
        consent: UserConsent
    ): String? {
        validateUserProfile(userProfileJson)
        val jsonObject = parseToJsonObject(userProfileJson)
        return updateJsonWithConsent(jsonObject, consent)
    }

    private fun validateUserProfile(userProfileJson: String) {
        if (!isJsonValid.value || userProfileJson == null) {
            Log.e(TAG, "Cannot update invalid JSON")
            throw IllegalArgumentException("Invalid JSON")
        }
    }

    private fun parseToJsonObject(jsonString: String): JSONObject {
        return try {
            JSONObject(jsonString)
        } catch (e: JSONException) {
            throw IllegalArgumentException("Invalid JSON format", e)
        }
    }

    private fun updateJsonWithConsent(jsonObject: JSONObject, consent: UserConsent): String {
        return jsonObject.apply {
            put(UserProfileKey.PRIVACY_POLICY_ENABLED.key, consent.privacyPolicy)
            put(UserProfileKey.TERMS_OF_SERVICE_ENABLED.key, consent.termsOfService)
        }.toString()
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
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "Failed to save user to Firestore", e)
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save user to Firestore", e)
            throw e
        }
    }

    fun extractUserInfo(userProfileJson: String): Pair<Map<String, Any?>, String> {
        return createInitialProfileUseCase.extractUserInfo(userProfileJson)
    }

    suspend fun saveUserToRoom(userProfileJson: String): String {
        try {
            val (profileMap, userId) = extractUserInfo(userProfileJson)
            return saveUserProfileUseCase.saveToRoom(profileMap, userId)
        } catch (e: SQLiteException) {
            Log.e(TAG, "Failed to save user to Room", e)
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save user to Room", e)
            throw e
        }
    }

    suspend fun syncExistingUser(userId: String) {
        saveUserProfileUseCase.syncExistingUser(userId)
    }
}
