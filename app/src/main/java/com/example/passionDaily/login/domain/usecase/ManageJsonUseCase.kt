package com.example.passionDaily.login.domain.usecase

import android.util.Log
import com.example.passionDaily.data.repository.remote.RemoteUserRepository
import com.example.passionDaily.login.UserProfileKey
import com.example.passionDaily.login.domain.model.UserConsent
import com.example.passionDaily.login.stateholder.UserProfileStateHolder
import com.example.passionDaily.mapper.UserProfileMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject

class ManageJsonUseCase @Inject constructor(
    private val userProfileMapper: UserProfileMapper,
    private val userProfileStateHolder: UserProfileStateHolder,
) {
    suspend fun verifyJson(json: String?): Boolean = withContext(Dispatchers.IO) {
        if (json.isNullOrEmpty()) {
            Log.e("UserProfileJsonManager", "Invalid JSON: null or empty")
            userProfileStateHolder.updateIsJsonValid(false)
            return@withContext false
        }

        return@withContext try {
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

    suspend fun updateUserProfileWithConsent(
        userProfileJson: String,
        consent: UserConsent
    ): String? = withContext(Dispatchers.Default) {
        validateUserProfile(userProfileJson)
        val jsonObject = parseToJsonObject(userProfileJson)
        updateJsonWithConsent(jsonObject, consent)
    }

    private fun validateUserProfile(userProfileJson: String) {
        if (!userProfileStateHolder.isJsonValid.value || userProfileJson == null) {
            Log.e("ManageJsonUseCase", "Cannot update invalid JSON")
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

    suspend fun extractUserInfo(userProfileJson: String): Pair<Map<String, Any?>, String> =
        withContext(Dispatchers.IO) {
            val profileMap = mapFromJson(userProfileJson)
            val userId = requireUserId(profileMap)
            Pair(profileMap, userId)
        }

    private fun mapFromJson(userProfileJson: String): Map<String, Any?> {
        return userProfileMapper.mapFromJson(userProfileJson)
    }

    private fun requireUserId(profileMap: Map<String, Any?>): String {
        return profileMap[UserProfileKey.ID.key] as? String
            ?: throw IllegalArgumentException("User ID not found in profile")
    }
}