package com.example.passionDaily.quote.data.remote.model

import com.example.passionDaily.quotecategory.model.QuoteCategory

data class Quote(
    val id: String,
    val category: QuoteCategory,
    val text: String,
    val person: String,
    val imageUrl: String,
    val createdAt: String,
    val modifiedAt: String,
    val shareCount: Int
)
