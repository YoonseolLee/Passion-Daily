package com.example.passionDaily.favorites.data.local.repository

import com.example.passionDaily.favorites.data.local.dao.FavoriteDao
import com.example.passionDaily.favorites.data.local.entity.FavoriteEntity
import com.example.passionDaily.quote.data.local.entity.QuoteEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class LocalFavoriteRepositoryImpl @Inject constructor(
    private val favoriteDao: FavoriteDao
) : LocalFavoriteRepository {

    override suspend fun getAllFavorites(): Flow<List<QuoteEntity>> {
        return favoriteDao.getAllFavorites()
    }

    override suspend fun insertFavorite(favorite: FavoriteEntity) {
        favoriteDao.insertFavorite(favorite)
    }

    override suspend fun deleteFavorite(quoteId: String, categoryId: Int) {
        favoriteDao.deleteFavorite(quoteId, categoryId)
    }

    override fun checkFavoriteEntity(
        quoteId: String,
        categoryId: Int
    ): Flow<FavoriteEntity?> {
        return favoriteDao.checkFavoriteEntity(quoteId, categoryId)
    }

    override suspend fun getFavoritesForQuote(quoteId: String, categoryId: Int): List<FavoriteEntity> {
        return favoriteDao.getFavoritesForQuote(quoteId, categoryId)
    }
}