package com.example.passionDaily.user.data.remote.model

import com.example.passionDaily.util.UserRole

data class User(
    val id: String = "",
    val email: String = "",
    val role: UserRole = UserRole.USER,
    val lastLoginDate: String = "",
    val notificationEnabled: Boolean = false,
    val notificationTime: String = "08:00",
    val lastSyncDate: String = "",
    val isAccountDeleted: Boolean = false,
    val createdDate: String = "",
    val modifiedDate: String = "",
)