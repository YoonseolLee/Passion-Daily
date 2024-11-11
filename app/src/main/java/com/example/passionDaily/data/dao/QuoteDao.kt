package com.example.passionDaily.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.passionDaily.data.entity.QuoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteDao {
    @Query("SELECT * FROM quotes")
    fun getAllQuotes(): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes WHERE category_id = :categoryId")
    fun getQuotesByCategory(categoryId: Int): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes WHERE quote_id = :quoteId")
    fun getQuoteById(quoteId: Int): Flow<QuoteEntity?>
}
