package com.example.passionDaily.data.repository.local

import com.example.passionDaily.data.local.dto.FavoriteWithCategory
import com.example.passionDaily.data.local.entity.FavoriteEntity
import com.example.passionDaily.data.local.entity.QuoteEntity
import kotlinx.coroutines.flow.Flow

interface LocalFavoriteRepository {
    suspend fun getAllFavorites(userId: String): Flow<List<QuoteEntity>>
    suspend fun insertFavorite(favorite: FavoriteEntity)
    suspend fun deleteFavorite(userId: String, quoteId: String, categoryId: Int)
    suspend fun deleteAllFavoritesByUserId(userId: String)
    suspend fun getAllFavoriteIds(userId: String): Flow<List<String>>
    suspend fun getAllFavoriteIdsWithCategory(userId: String): Flow<List<FavoriteWithCategory>>
    fun checkFavoriteEntity(userId: String, quoteId: String, categoryId: Int): Flow<FavoriteEntity?>
    suspend fun getFavoritesForQuote(quoteId: String, categoryId: Int): List<FavoriteEntity>
}