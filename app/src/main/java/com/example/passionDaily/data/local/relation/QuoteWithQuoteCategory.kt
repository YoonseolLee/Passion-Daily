package com.example.passionDaily.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.passionDaily.data.local.entity.QuoteCategoryEntity
import com.example.passionDaily.data.local.entity.QuoteEntity

data class QuoteWithQuoteCategory(
    @Embedded val quote: QuoteEntity,
    @Relation(
        parentColumn = "category_id",
        entityColumn = "category_id",
    )
    val category: QuoteCategoryEntity,
)
