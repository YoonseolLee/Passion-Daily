package com.example.passionDaily.data.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.passionDaily.data.entity.NotificationEntity
import com.example.passionDaily.data.entity.UserEntity

data class UserWithNotification(
    @Embedded val user: UserEntity,
    @Relation(
        parentColumn = "user_id",
        entityColumn = "user_id",
    )
    val notification: NotificationEntity?,
)
