package com.example.passionDaily.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: Int,
    val username: String,
    val nickname: String?,
    val email: String,
    val role: String,
    val gender: String?,
    val birthYear: Int?,
    val isAccountDeleted: Boolean,
    val lastLoginDate: Date?,
    val createdDate: Date,
    val modifiedDate: Date,
)
