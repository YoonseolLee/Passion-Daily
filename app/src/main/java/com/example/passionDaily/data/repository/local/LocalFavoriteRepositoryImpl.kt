package com.example.passionDaily.data.repository.local

import com.example.passionDaily.data.local.dao.FavoriteDao
import com.example.passionDaily.data.local.dto.FavoriteWithCategory
import com.example.passionDaily.data.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class LocalFavoriteRepositoryImpl @Inject constructor(
    private val favoriteDao: FavoriteDao
) : LocalFavoriteRepository {
    override suspend fun getAllFavorites(): Flow<List<FavoriteEntity>> {
        TODO("Not yet implemented")
    }

    override suspend fun insertFavorite(favorite: FavoriteEntity) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteFavorite(favorite: FavoriteEntity) {
        TODO("Not yet implemented")
    }

    override suspend fun isQuoteFavorite(quoteId: String): Flow<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAllFavoritesByUserId(userId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun getAllFavoriteIds(userId: String): Flow<List<String>> {
        TODO("Not yet implemented")
    }

    override suspend fun getAllFavoriteIdsWithCategory(userId: String): Flow<List<FavoriteWithCategory>> {
        return favoriteDao.getAllFavoriteIdsWithCategory(userId)
    }
}