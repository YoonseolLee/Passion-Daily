package com.example.passionDaily.favorites.manager

import com.example.passionDaily.quote.data.local.entity.QuoteEntity
import kotlinx.coroutines.flow.Flow

interface FavoritesLoadingManager {
    fun updateFavoriteQuotes(quotes: List<QuoteEntity>)
    fun updateIsFavoriteLoading(isLoading: Boolean)
    suspend fun getAllFavorites(): Flow<List<QuoteEntity>>
    fun checkIfQuoteIsFavorite(quoteId: String, categoryId: Int): Flow<Boolean>
}