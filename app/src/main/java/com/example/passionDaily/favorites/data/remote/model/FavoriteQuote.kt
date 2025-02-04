package com.example.passionDaily.favorites.data.remote.model

import com.example.passionDaily.quotecategory.model.QuoteCategory

data class FavoriteQuote (
    val addedAt: String,
    val category: QuoteCategory,
    val quoteId: String,
)
