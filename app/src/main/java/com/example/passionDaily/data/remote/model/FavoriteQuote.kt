package com.example.passionDaily.data.remote.model

import com.example.passionDaily.util.QuoteCategory

data class FavoriteQuote (
    val addedAt: String,
    val category: QuoteCategory,
    val quoteId: String,
)
