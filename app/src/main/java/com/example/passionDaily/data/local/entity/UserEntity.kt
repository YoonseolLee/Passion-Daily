package com.example.passionDaily.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.passionDaily.util.AgeGroup
import com.example.passionDaily.util.Gender

/**
 *
 * 변수명은 카멜케이스, DB에 저장되는 변수는 스네이크 케이스로 저장.
 */

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "nickname") val nickname: String,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "gender") val gender: Gender,
    @ColumnInfo(name = "age_group") val ageGroup: AgeGroup,
    @ColumnInfo(name = "notification_enabled") val notificationEnabled: Boolean,
    @ColumnInfo(name = "last_sync_date") val lastSyncDate: Long,
    @ColumnInfo(name = "is_account_deleted") val isAccountDeleted: Boolean,
)
