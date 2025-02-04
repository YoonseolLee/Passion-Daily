package com.example.passionDaily.favorites.data.local.repository

import com.example.passionDaily.favorites.data.local.entity.FavoriteEntity
import com.example.passionDaily.quote.data.local.entity.QuoteEntity
import kotlinx.coroutines.flow.Flow

interface LocalFavoriteRepository {
    suspend fun getAllFavorites(userId: String): Flow<List<QuoteEntity>>
    suspend fun insertFavorite(favorite: FavoriteEntity)
    suspend fun deleteFavorite(userId: String, quoteId: String, categoryId: Int)
    suspend fun deleteAllFavoritesByUserId(userId: String)
    fun checkFavoriteEntity(userId: String, quoteId: String, categoryId: Int): Flow<FavoriteEntity?>
    suspend fun getFavoritesForQuote(quoteId: String, categoryId: Int): List<FavoriteEntity>
}