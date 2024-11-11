package com.example.passionDaily.data.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.passionDaily.data.entity.FavoriteEntity
import com.example.passionDaily.data.entity.UserEntity

data class UserWithFavoritesAndQuotes(
    @Embedded val user: UserEntity,
    @Relation(
        entity = FavoriteEntity::class,
        parentColumn = "userId",
        entityColumn = "userId",
    )
    val favorites: List<FavoriteWithQuotes>,
)
