package com.example.passionDaily.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.passionDaily.util.Gender
import com.example.passionDaily.util.UserRole

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: Int,
    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "nickname") val nickname: String?,
    @ColumnInfo(name = "phone") val phone: String?,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "role") val role: UserRole,
    @ColumnInfo(name = "gender") val gender: Gender?,
    @ColumnInfo(name = "birth_year") val birthYear: Int?,
    @ColumnInfo(name = "is_account_deleted") val isAccountDeleted: Boolean,
    @ColumnInfo(name = "last_login_date") val lastLoginDate: Long?,
    @ColumnInfo(name = "created_date") val createdDate: Long,
    @ColumnInfo(name = "modified_date") val modifiedDate: Long,
)
