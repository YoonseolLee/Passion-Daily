package com.example.passionDaily.data.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.passionDaily.data.entity.FavoriteEntity
import com.example.passionDaily.data.entity.QuoteEntity
import com.example.passionDaily.data.entity.UserEntity

data class UserWithFavoriteQuotes(
    @Embedded val user: UserEntity,
    @Relation(
        entity = QuoteEntity::class,
        parentColumn = "user_id",
        entityColumn = "quote_id",
        associateBy =
            Junction(
                value = FavoriteEntity::class,
                parentColumn = "user_id",
                entityColumn = "quote_id",
            ),
    )
    val favoriteQuotes: List<QuoteEntity>,
)
