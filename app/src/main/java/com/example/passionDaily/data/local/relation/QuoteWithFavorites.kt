package com.example.passionDaily.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.passionDaily.data.local.entity.FavoriteEntity
import com.example.passionDaily.data.local.entity.QuoteEntity

data class QuoteWithFavorites(
    @Embedded val quote: QuoteEntity,
    @Relation(
        parentColumn = "quote_id",
        entityColumn = "quote_id"
    )
    val favorites: List<FavoriteEntity>
)