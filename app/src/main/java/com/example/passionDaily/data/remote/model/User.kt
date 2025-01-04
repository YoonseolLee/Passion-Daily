package com.example.passionDaily.data.remote.model

import com.example.passionDaily.util.AgeGroup
import com.example.passionDaily.util.Gender
import com.example.passionDaily.util.UserRole
import com.google.firebase.Timestamp

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