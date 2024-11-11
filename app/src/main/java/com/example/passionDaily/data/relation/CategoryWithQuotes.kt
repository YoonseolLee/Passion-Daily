package com.example.passionDaily.data.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.passionDaily.data.entity.QuoteCategoryEntity
import com.example.passionDaily.data.entity.QuoteEntity

data class CategoryWithQuotes(
    @Embedded val category: QuoteCategoryEntity,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "categoryId",
    )
    val quotes: List<QuoteEntity>,
)
