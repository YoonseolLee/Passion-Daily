package com.example.passionDaily.data.repository

import com.example.passionDaily.data.dao.QuoteDao
import com.example.passionDaily.data.entity.QuoteEntity
import com.example.passionDaily.data.relation.QuoteWithCategory
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class QuoteRepository @Inject constructor(private val quoteDao: QuoteDao) {

    suspend fun getQuoteById(quoteId: Int): QuoteEntity? {
        return quoteDao.getQuoteById(quoteId)
    }

    suspend fun getQuotesByIds(quoteIds: List<Int>): List<QuoteEntity> {
        return quoteDao.getQuotesByIds(quoteIds)
    }


    fun getQuotesByCategory(categoryId: Int): Flow<List<QuoteEntity>> {
        return quoteDao.getQuotesByCategory(categoryId)
    }

    suspend fun insertQuote(quote: QuoteEntity) {
        quoteDao.insertQuote(quote)
    }

    suspend fun updateQuote(quote: QuoteEntity) {
        quoteDao.updateQuote(quote)
    }

    suspend fun deleteQuote(quote: QuoteEntity) {
        quoteDao.deleteQuote(quote)
    }

    suspend fun getQuoteWithCategory(quoteId: Int): QuoteWithCategory? {
        return quoteDao.getQuoteWithCategory(quoteId)
    }
}