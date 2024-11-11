package com.example.passionDaily.data.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.passionDaily.data.entity.FavoriteEntity
import com.example.passionDaily.data.entity.QuoteEntity

data class FavoriteWithQuotes(
    @Embedded val favorite: FavoriteEntity,
    @Relation(
        parentColumn = "quoteId",
        entityColumn = "quoteId",
    )
    val quote: QuoteEntity,
)
