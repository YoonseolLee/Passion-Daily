package com.example.passionDaily.quote.data.local

import com.example.passionDaily.data.local.dao.QuoteDao
import com.example.passionDaily.data.local.entity.QuoteEntity
import com.example.passionDaily.data.local.relation.QuoteWithQuoteCategory
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalQuoteRepositoryImpl @Inject constructor(
    private val quoteDao: QuoteDao
) : LocalQuoteRepository {

    override suspend fun getQuoteById(quoteId: String): QuoteEntity? {
        return quoteDao.getQuoteById(quoteId)
    }

    override suspend fun getQuotesByIds(quoteIds: List<String>): List<QuoteEntity> {
        return quoteDao.getQuotesByIds(quoteIds)
    }

    override suspend fun getQuotesByCategory(categoryId: String): Flow<List<QuoteEntity>> {
        return quoteDao.getQuotesByCategory(categoryId)
    }

    override suspend fun insertQuote(quote: QuoteEntity) {
        quoteDao.insertQuote(quote)
    }

    override suspend fun updateQuote(quote: QuoteEntity) {
        quoteDao.updateQuote(quote)
    }

    override suspend fun deleteQuote(quoteId: String, categoryId: Int) {
        quoteDao.deleteQuote(quoteId, categoryId)
    }

    override suspend fun getQuoteWithCategory(quoteId: String): QuoteWithQuoteCategory? {
        return quoteDao.getQuoteWithCategory(quoteId)
    }

    override suspend fun isQuoteExistsInCategory(quoteId: String, categoryId: Int): Boolean {
        return quoteDao.isQuoteExistsInCategory(quoteId, categoryId)
    }

    override suspend fun deleteAllQuotes() {
        quoteDao.deleteAllQuotes()
    }
}