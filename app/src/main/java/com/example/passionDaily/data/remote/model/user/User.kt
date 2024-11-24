package com.example.passionDaily.data.remote.model.user

import com.google.firebase.Timestamp


data class User(
    val id: String = "", // firebase_uid
    val username: String = "",
    val nickname: String = "",
    val email: String = "",
    val role: UserRole = UserRole.USER,
    val gender: Gender = Gender.M,
    val birthYear: Int = 0,
    val lastLoginDate: Timestamp = Timestamp.now(),
    val notificationEnabled: Boolean = true,
    val promotionEnabled: Boolean = false,
    val lastSyncDate: Timestamp = Timestamp.now(),
    val isAccountDeleted: Boolean = false,
    val createdDate: Timestamp = Timestamp.now(),
    val modifiedDate: Timestamp = Timestamp.now()
) {
    enum class UserRole {
        USER, ADMIN
    }

    enum class Gender {
        M, F
    }
}