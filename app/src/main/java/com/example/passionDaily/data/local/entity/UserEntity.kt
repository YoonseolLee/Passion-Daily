package com.example.passionDaily.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.passionDaily.util.AuthProvider

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey @ColumnInfo(name = "user_id") val userId: Int,
    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "nickname") val nickname: String?,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "is_account_deleted") val isAccountDeleted: Boolean,
    @ColumnInfo(name = "created_date") val createdDate: Long,
    @ColumnInfo(name = "modified_date") val modifiedDate: Long,
    @ColumnInfo(name = "auth_provider") val authProvider: AuthProvider,
)