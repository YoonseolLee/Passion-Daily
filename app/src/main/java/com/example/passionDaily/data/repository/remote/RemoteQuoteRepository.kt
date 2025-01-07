package com.example.passionDaily.data.repository.remote

import com.example.passionDaily.data.remote.model.Quote
import com.example.passionDaily.util.QuoteCategory
import com.google.firebase.firestore.DocumentSnapshot

interface RemoteQuoteRepository {
    suspend fun getQuotes(category: QuoteCategory, lastQuote: DocumentSnapshot?, pageSize: Int): List<Quote>
    suspend fun getFavoriteQuotes(): List<Quote>
    suspend fun addToFavorites(quote: Quote, userId: String)
    suspend fun removeFromFavorites(quoteId: String, userId: String)
    suspend fun incrementShareCount(quoteId: String, category: QuoteCategory)
}