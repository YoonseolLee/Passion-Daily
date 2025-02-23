package com.example.passionDaily.favorites.manager

import com.example.passionDaily.quote.data.remote.model.Quote
import com.example.passionDaily.quotecategory.model.QuoteCategory
import com.google.firebase.auth.FirebaseUser

interface FavoritesSavingManager {
    suspend fun saveToLocalDatabase(
        selectedCategory: QuoteCategory,
        currentQuote: Quote
    )

    fun getRequiredDataForAdd(
        selectedCategory: QuoteCategory,
        quotes: List<Quote>,
        quoteId: String
    ): Pair<QuoteCategory, Quote>?
}