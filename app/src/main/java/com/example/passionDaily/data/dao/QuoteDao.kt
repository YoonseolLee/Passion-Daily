package com.example.passionDaily.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.passionDaily.data.entity.QuoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteDao {
    @Query("SELECT * FROM quotes WHERE quoteId = :quoteId")
    fun getQuoteById(quoteId: Int): Flow<QuoteEntity?>

    @Query("SELECT * FROM quotes")
    fun getAllQuotes(): Flow<List<QuoteEntity>>
}
