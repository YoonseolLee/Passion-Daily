package com.example.passionDaily.data.repository.remote

import com.example.passionDaily.data.remote.model.Quote
import com.example.passionDaily.util.QuoteCategory
import com.google.firebase.firestore.DocumentSnapshot

interface RemoteQuoteRepository {
    suspend fun getQuotesByCategory(category: QuoteCategory, pageSize: Int, lastLoadedQuote: DocumentSnapshot?): RemoteQuoteRepositoryImpl.QuoteResult
    suspend fun incrementShareCount(quoteId: String, category: QuoteCategory)
    suspend fun getQuoteById(quoteId: String, category: QuoteCategory): Quote?
    suspend fun getQuotesBeforeId(category: QuoteCategory, targetQuoteId: String, limit: Int): List<Quote>
    suspend fun getQuotesAfterId(category: QuoteCategory, afterQuoteId: String, limit: Int): RemoteQuoteRepositoryImpl.QuoteResult
}