package com.example.passionDaily.quote.data.remote.repository

import com.example.passionDaily.quote.data.remote.model.Quote
import com.example.passionDaily.quote.domain.model.QuoteResult
import com.example.passionDaily.quotecategory.model.QuoteCategory
import com.google.firebase.firestore.DocumentSnapshot

interface RemoteQuoteRepository {
    suspend fun getQuotesByCategory(category: QuoteCategory, pageSize: Int, lastLoadedQuote: DocumentSnapshot?): QuoteResult
    suspend fun incrementShareCount(quoteId: String, category: QuoteCategory)
    suspend fun getQuoteById(quoteId: String, category: QuoteCategory): Quote?
    suspend fun getQuotesBeforeId(category: QuoteCategory, targetQuoteId: String, limit: Int): List<Quote>
    suspend fun getQuotesAfterId(category: QuoteCategory, afterQuoteId: String, limit: Int): QuoteResult
}