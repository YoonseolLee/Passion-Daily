package com.example.passionDaily.user.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "notification_enabled") val notificationEnabled: Boolean,
    @ColumnInfo(name = "notification_time") val notificationTime: String,
    @ColumnInfo(name = "last_sync_date") val lastSyncDate: Long,
)
