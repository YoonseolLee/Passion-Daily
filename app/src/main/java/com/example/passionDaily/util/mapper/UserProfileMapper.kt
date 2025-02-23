package com.example.passionDaily.util.mapper

import com.example.passionDaily.login.domain.model.UserProfileKey
import com.example.passionDaily.util.Converters
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.internal.`$Gson$Types`
import java.lang.reflect.Type
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 제네릭 타입을 유지하면서 ProGuard와 호환되는 방식으로 JSON 매핑을 처리하는 클래스
 */
@Singleton
class UserProfileMapper @Inject constructor() {
    private val gson: Gson = GsonBuilder().serializeNulls().create()

    // Gson 내부 API를 사용하여 타입 생성 - ProGuard와 더 잘 작동함
    private val mapType: Type = `$Gson$Types`.newParameterizedTypeWithOwner(
        null,
        Map::class.java,
        String::class.java,
        Any::class.java
    )

    /**
     * Map을 JSON 문자열로 변환
     */
    fun mapToJson(profile: Map<String, Any?>): String {
        return gson.toJson(profile)
    }

    /**
     * JSON 문자열을 Map으로 변환
     */
    fun mapFromJson(json: String): Map<String, Any?> {
        @Suppress("UNCHECKED_CAST")
        return gson.fromJson(json, mapType) as Map<String, Any?>
    }

    /**
     * 프로필 맵을 UserEntity로 변환
     */
    fun mapToUserEntity(profileMap: Map<String, Any?>): UserEntity {
        try {
            return UserEntity(
                userId = profileMap[UserProfileKey.ID.key] as String,
                name = profileMap[UserProfileKey.NAME.key] as String,
                notificationEnabled = profileMap[UserProfileKey.NOTIFICATION_ENABLED.key] as Boolean,
                lastSyncDate = Converters.fromStringToLong(profileMap[UserProfileKey.LAST_SYNC_DATE.key] as String),
                notificationTime = profileMap[UserProfileKey.NOTIFICATION_TIME.key] as String
            )
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Failed to map user profile", e)
        }
    }

    /**
     * Map을 JSON으로 변환 - mapToJson과 동일하지만 네이밍 일관성을 위해 유지
     */
    fun convertMapToJson(map: Map<String, Any?>): String {
        return gson.toJson(map)
    }
}