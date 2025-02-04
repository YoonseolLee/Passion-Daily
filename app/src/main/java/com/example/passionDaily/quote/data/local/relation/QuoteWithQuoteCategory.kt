package com.example.passionDaily.quote.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.passionDaily.quotecategory.data.local.entity.QuoteCategoryEntity
import com.example.passionDaily.quote.data.local.entity.QuoteEntity

data class QuoteWithQuoteCategory(
    @Embedded val quote: QuoteEntity,
    @Relation(
        parentColumn = "category_id",
        entityColumn = "category_id",
    )
    val category: QuoteCategoryEntity,
)
