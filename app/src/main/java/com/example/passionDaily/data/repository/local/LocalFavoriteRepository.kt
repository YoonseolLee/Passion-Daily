package com.example.passionDaily.data.repository.local

import com.example.passionDaily.data.local.dto.FavoriteWithCategory
import com.example.passionDaily.data.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

interface LocalFavoriteRepository {
    suspend fun getAllFavorites(): Flow<List<FavoriteEntity>>
    suspend fun insertFavorite(favorite: FavoriteEntity)
    suspend fun deleteFavorite(favorite: FavoriteEntity)
    suspend fun isQuoteFavorite(quoteId: String): Flow<Boolean>
    suspend fun deleteAllFavoritesByUserId(userId: String)
    suspend fun getAllFavoriteIds(userId: String): Flow<List<String>>
    suspend fun getAllFavoriteIdsWithCategory(userId: String):  Flow<List<FavoriteWithCategory>>
}