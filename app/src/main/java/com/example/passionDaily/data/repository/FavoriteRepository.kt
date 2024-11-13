package com.example.passionDaily.data.repository

import com.example.passionDaily.data.dao.FavoriteDao
import com.example.passionDaily.data.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FavoriteRepository @Inject constructor(private val favoriteDao: FavoriteDao) {
    fun getFavoritesByUserId(userId: Int): Flow<List<FavoriteEntity>> {
        return favoriteDao.getFavoritesByUserId(userId)
    }

    suspend fun isFavorite(userId: Int, quoteId: Int): Boolean {
        return favoriteDao.isFavorite(userId, quoteId)
    }

    suspend fun insertFavorite(favorite: FavoriteEntity) {
        favoriteDao.insertFavorite(favorite)
    }

    suspend fun updateFavorite(favorite: FavoriteEntity) {
        favoriteDao.updateFavorite(favorite)
    }

    suspend fun deleteFavorite(userId: Int, quoteId: Int) {
        favoriteDao.deleteFavorite(userId, quoteId)
    }
}