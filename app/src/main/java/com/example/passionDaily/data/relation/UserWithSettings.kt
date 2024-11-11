package com.example.passionDaily.data.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.passionDaily.data.entity.UserEntity
import com.example.passionDaily.data.entity.UserSettingsEntity

data class UserWithSettings(
    @Embedded val user: UserEntity,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId",
    )
    val settings: UserSettingsEntity,
)
