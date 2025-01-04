package com.example.passionDaily.data.remote.dto

import com.example.passionDaily.util.AgeGroup
import com.example.passionDaily.util.Gender
import com.example.passionDaily.util.UserRole
import com.google.firebase.Timestamp

data class UserDTO(
    val id: String,
    val email: String?,
    val role: UserRole,
    val lastLoginDate: Timestamp,
    val notificationEnabled: Boolean,
    val promotionEnabled: Boolean,
    val lastSyncDate: Timestamp,
    val isAccountDeleted: Boolean,
    val createdDate: Timestamp,
    val modifiedDate: Timestamp
)