package com.example.passionDaily.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.passionDaily.data.local.entity.FavoriteEntity
import com.example.passionDaily.data.local.entity.UserEntity

data class UserWithFavorites(
    @Embedded val user: UserEntity,
    @Relation(
        parentColumn = "user_id",
        entityColumn = "user_id",
    )
    val favorites: List<FavoriteEntity>,
)
