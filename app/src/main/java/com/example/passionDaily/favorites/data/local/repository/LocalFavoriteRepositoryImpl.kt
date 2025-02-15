package com.example.passionDaily.favorites.data.local.repository

import com.example.passionDaily.favorites.data.local.dao.FavoriteDao
import com.example.passionDaily.favorites.data.local.entity.FavoriteEntity
import com.example.passionDaily.quote.data.local.entity.QuoteEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class LocalFavoriteRepositoryImpl @Inject constructor(
    private val favoriteDao: FavoriteDao
) : LocalFavoriteRepository {

    /**
     * 1. Flow를 반환하는 함수에는 withContext(Dispatchers.IO)를 쓰지 않는다.
     * 2. Room DAO의 suspend fun들은 기본적으로 IO에서 실행된다.
     */

    override suspend fun getAllFavorites(userId: String): Flow<List<QuoteEntity>> {
        return favoriteDao.getAllFavorites(userId)
    }

    override suspend fun insertFavorite(favorite: FavoriteEntity) {
        favoriteDao.insertFavorite(favorite)
    }

    override suspend fun deleteFavorite(userId: String, quoteId: String, categoryId: Int) {
        favoriteDao.deleteFavorite(userId, quoteId, categoryId)
    }

    override suspend fun deleteAllFavoritesByUserId(userId: String) {
        favoriteDao.deleteAllFavoritesByUserId(userId)
    }

    override fun checkFavoriteEntity(
        userId: String,
        quoteId: String,
        categoryId: Int
    ): Flow<FavoriteEntity?> {
        return favoriteDao.checkFavoriteEntity(userId, quoteId, categoryId)
    }

    override suspend fun getFavoritesForQuote(quoteId: String, categoryId: Int): List<FavoriteEntity> {
        return favoriteDao.getFavoritesForQuote(quoteId, categoryId)
    }
}