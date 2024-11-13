package com.example.passionDaily.data.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.passionDaily.data.entity.QuoteCategoryEntity
import com.example.passionDaily.data.entity.QuoteEntity

data class QuoteWithCategory(
    @Embedded val quote: QuoteEntity,
    @Relation(
        parentColumn = "category_id",
        entityColumn = "category_id",
    )
    val category: QuoteCategoryEntity,
)
