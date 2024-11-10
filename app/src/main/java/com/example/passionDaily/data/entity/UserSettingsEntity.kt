package com.example.passionDaily.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val userId: Int,
    val isPushEnabled: Boolean,
    val isPromotionalEnabled: Boolean,
    val lastSyncDate: Date,
)
