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
        favoriteDao.insertFavorite(favorite)
    }

    override suspend fun deleteFavorite(userId: String, quoteId: String, categoryId: Int) {
        favoriteDao.deleteFavorite(userId, quoteId, categoryId)
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

    override fun checkFavoriteEntity(userId: String, quoteId: String, categoryId: Int): Flow<FavoriteEntity?> {
        return favoriteDao.checkFavoriteEntity(userId, quoteId, categoryId)
    }
}