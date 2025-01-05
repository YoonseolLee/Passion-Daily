package com.example.passionDaily.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.passionDaily.util.AgeGroup
import com.example.passionDaily.util.Gender

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "notification_enabled") val notificationEnabled: Boolean,
    @ColumnInfo(name = "notification_time") val notificationTime: String,
    @ColumnInfo(name = "last_sync_date") val lastSyncDate: Long,
)
