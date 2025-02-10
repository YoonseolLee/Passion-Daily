package com.example.passionDaily.favorites.manager

import com.example.passionDaily.quote.data.remote.model.Quote
import com.example.passionDaily.quotecategory.model.QuoteCategory
import com.google.firebase.auth.FirebaseUser

interface FavoritesSavingManager {
    suspend fun saveToLocalDatabase(
        currentUser: FirebaseUser,
        selectedCategory: QuoteCategory,
        currentQuote: Quote
    )

    fun getRequiredDataForAdd(
        currentUser: FirebaseUser?,
        selectedCategory: QuoteCategory,
        quotes: List<Quote>,
        quoteId: String
    ): Triple<FirebaseUser, QuoteCategory, Quote>?

    suspend fun addFavoriteToFirestore(
        currentUser: FirebaseUser,
        quoteId: String,
        selectedCategory: QuoteCategory
    )
}