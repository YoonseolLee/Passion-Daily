package com.example.passionDaily.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "user_settings",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE,
        )
    ]
)
data class UserSettingsEntity(
    @PrimaryKey val userId: Int,
    val isPushEnabled: Boolean = true,
    val isPromotionalEnabled: Boolean = false,
    val lastSyncDate: Date,
)
