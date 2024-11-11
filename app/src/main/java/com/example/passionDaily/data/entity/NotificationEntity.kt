package com.example.passionDaily.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "notifications",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class NotificationEntity(
    @PrimaryKey @ColumnInfo(name = "user_id") val userId: Int,
    @ColumnInfo(name = "is_push_enabled") val isPushEnabled: Boolean,
    @ColumnInfo(name = "is_promotional_enabled") val isPromotionalEnabled: Boolean,
    @ColumnInfo(name = "last_sync_date") val lastSyncDate: Long,
)
