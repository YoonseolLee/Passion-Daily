package com.example.passionDaily.quote.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.passionDaily.quote.data.local.entity.QuoteEntity
import com.example.passionDaily.quote.data.local.relation.QuoteWithQuoteCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteDao {
    @Query("SELECT * FROM quotes WHERE quote_id = :quoteId")
    suspend fun getQuoteById(quoteId: String): QuoteEntity?

    @Query("SELECT * FROM quotes WHERE quote_id IN (:quoteIds)")
    suspend fun getQuotesByIds(quoteIds: List<String>): List<QuoteEntity>

    @Query("SELECT * FROM quotes WHERE category_id = :categoryId")
    fun getQuotesByCategory(categoryId: String): Flow<List<QuoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: QuoteEntity)

    @Update
    suspend fun updateQuote(quote: QuoteEntity)

    @Query("DELETE FROM quotes WHERE quote_id = :quoteId AND category_id = :categoryId")
    suspend fun deleteQuote(quoteId: String, categoryId: Int)

    @Transaction
    @Query("SELECT * FROM quotes WHERE quote_id = :quoteId")
    suspend fun getQuoteWithCategory(quoteId: String): QuoteWithQuoteCategory?

    @Query("DELETE FROM quotes")
    suspend fun deleteAllQuotes()

    @Query("SELECT EXISTS(SELECT 1 FROM quotes WHERE quote_id = :quoteId AND category_id = :categoryId)")
    suspend fun isQuoteExistsInCategory(quoteId: String, categoryId: Int): Boolean
}
