package com.example.passionDaily.quote.data.local

import com.example.passionDaily.data.local.entity.QuoteEntity
import com.example.passionDaily.data.local.relation.QuoteWithQuoteCategory
import kotlinx.coroutines.flow.Flow

interface LocalQuoteRepository {

    suspend fun getQuoteById(quoteId: String): QuoteEntity?
    suspend fun getQuotesByIds(quoteIds: List<String>): List<QuoteEntity>
    suspend fun getQuotesByCategory(categoryId: String): Flow<List<QuoteEntity>>
    suspend fun insertQuote(quote: QuoteEntity)
    suspend fun updateQuote(quote: QuoteEntity)
    suspend fun deleteQuote(quoteId: String, categoryId: Int)
    suspend fun getQuoteWithCategory(quoteId: String): QuoteWithQuoteCategory?
    suspend fun deleteAllQuotes()
    suspend fun isQuoteExistsInCategory(quoteId: String, categoryId: Int): Boolean

}