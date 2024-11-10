package com.example.passionDaily.data.repository

import com.example.passionDaily.data.dao.FavoriteDao
import com.example.passionDaily.data.dao.QuoteCategoryDao
import com.example.passionDaily.data.dao.QuoteDao
import com.example.passionDaily.data.dao.UserDao
import com.example.passionDaily.data.dao.UserSettingsDao
import com.example.passionDaily.data.entity.FavoriteEntity
import com.example.passionDaily.data.entity.QuoteCategoryEntity
import com.example.passionDaily.data.entity.QuoteEntity
import com.example.passionDaily.data.entity.UserEntity
import com.example.passionDaily.data.entity.UserSettingsEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PassionDailyRepository
    @Inject
    constructor(
        private val userDao: UserDao,
        private val quoteDao: QuoteDao,
        private val categoryDao: QuoteCategoryDao,
        private val favoriteDao: FavoriteDao,
        private val userSettingsDao: UserSettingsDao,
    ) {
        fun getAllQuotes(): Flow<List<QuoteEntity>> = quoteDao.getAllQuotes()

        fun getFavoritesByUserId(userId: Int): Flow<List<FavoriteEntity>> = favoriteDao.getFavoritesByUserId(userId)

        suspend fun getUserById(userId: Int): Flow<UserEntity?> = userDao.getUserById(userId)

        fun getAllCategories(): List<QuoteCategoryEntity> = categoryDao.getAllCategories()

        suspend fun getUserSettings(userId: Int): UserSettingsEntity? = userSettingsDao.getUserSettings(userId)

        suspend fun addFavorite(favorite: FavoriteEntity) = favoriteDao.addFavorite(favorite)

        suspend fun deleteFavorite(favorite: FavoriteEntity) = favoriteDao.deleteFavorite(favorite)

        suspend fun updateSettings(settings: UserSettingsEntity) = userSettingsDao.updateSettings(settings)
    }
