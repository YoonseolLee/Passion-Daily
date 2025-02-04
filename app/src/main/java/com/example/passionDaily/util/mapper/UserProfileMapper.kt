package com.example.passionDaily.util.mapper

import com.example.passionDaily.user.data.local.entity.UserEntity
import com.example.passionDaily.login.domain.model.UserProfileKey
import com.example.passionDaily.util.Converters
import com.google.common.reflect.TypeToken
import com.google.gson.GsonBuilder
import javax.inject.Inject

class UserProfileMapper @Inject constructor() {
    private val gson = GsonBuilder().serializeNulls().create()

    fun mapToJson(profile: Map<String, Any?>): String {
        return gson.toJson(profile)
    }

    fun mapFromJson(json: String): Map<String, Any?> {
        return gson.fromJson(json, object : TypeToken<Map<String, Any?>>() {}.type)
    }

    fun mapToUserEntity(profileMap: Map<String, Any?>): UserEntity {
        try {
            return UserEntity(
                userId = profileMap[UserProfileKey.ID.key] as String,
                email = profileMap[UserProfileKey.EMAIL.key] as String,
                notificationEnabled = profileMap[UserProfileKey.NOTIFICATION_ENABLED.key] as Boolean,
                lastSyncDate = Converters.fromStringToLong(profileMap[UserProfileKey.LAST_SYNC_DATE.key] as String),
                notificationTime = profileMap[UserProfileKey.NOTIFICATION_TIME.key] as String
            )
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Failed to map user profile", e)
        }
    }

    fun convertMapToJson(map: Map<String, Any?>): String {
        val gson = GsonBuilder()
            .serializeNulls()
            .create()
        return gson.toJson(map)
    }
}