package com.example.passionDaily.quote.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.passionDaily.favorites.data.local.entity.FavoriteEntity
import com.example.passionDaily.quote.data.local.entity.QuoteEntity

data class QuoteWithFavorites(
    @Embedded val quote: QuoteEntity,
    @Relation(
        parentColumn = "quote_id",
        entityColumn = "quote_id"
    )
    val favorites: List<FavoriteEntity>
)