package com.example.passionDaily.mapper

import com.example.passionDaily.data.local.entity.UserEntity
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
        return UserEntity(
            userId = profileMap["id"] as String,
            email = profileMap["email"] as String,
            notificationEnabled = profileMap["notificationEnabled"] as Boolean,
            lastSyncDate = (profileMap["lastSyncDate"] as String).let {
                Converters.fromStringToLong(it)
            },
            notificationTime = profileMap["notificationTime"] as String,
        )
    }

    fun convertMapToJson(map: Map<String, Any?>): String {
        val gson = GsonBuilder()
            .serializeNulls()
            .create()
        return gson.toJson(map)
    }
}