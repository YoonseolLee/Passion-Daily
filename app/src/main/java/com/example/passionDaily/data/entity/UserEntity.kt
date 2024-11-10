package com.example.passionDaily.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["username"], unique = true)
    ]
)
data class UserEntity(
    @PrimaryKey val userId: Int,
    val username: String,
    val nickname: String?,
    val email: String,
    val role: String,
    val gender: String?,
    val birthYear: Int?,
    val isAccountDeleted: Boolean = false,
    val lastLoginDate: Date?,
    val createdDate: Date,
    val modifiedDate: Date,
)
