package com.example.passionDaily.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.passionDaily.data.local.entity.QuoteEntity
import com.example.passionDaily.data.local.relation.QuoteWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteDao {
    @Query("SELECT * FROM quotes WHERE quote_id = :quoteId")
    suspend fun getQuoteById(quoteId: Int): QuoteEntity?

    @Query("SELECT * FROM quotes WHERE quote_id IN (:quoteIds)")
    suspend fun getQuotesByIds(quoteIds: List<Int>): List<QuoteEntity>

    @Query("SELECT * FROM quotes WHERE category_id = :categoryId")
    fun getQuotesByCategory(categoryId: Int): Flow<List<QuoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: QuoteEntity)

    @Update
    suspend fun updateQuote(quote: QuoteEntity)

    @Delete
    suspend fun deleteQuote(quote: QuoteEntity)

    @Transaction
    @Query("SELECT * FROM quotes WHERE quote_id = :quoteId")
    suspend fun getQuoteWithCategory(quoteId: Int): QuoteWithCategory?
}
