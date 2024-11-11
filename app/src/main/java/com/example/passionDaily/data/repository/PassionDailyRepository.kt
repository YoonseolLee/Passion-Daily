package com.example.passionDaily.data.repository

import com.example.passionDaily.data.dao.FavoriteDao
import com.example.passionDaily.data.dao.NotificationDao
import com.example.passionDaily.data.dao.QuoteCategoryDao
import com.example.passionDaily.data.dao.QuoteDao
import com.example.passionDaily.data.dao.TermsConsentDao
import com.example.passionDaily.data.dao.UserDao
import com.example.passionDaily.data.entity.FavoriteEntity
import com.example.passionDaily.data.entity.NotificationEntity
import com.example.passionDaily.data.entity.QuoteCategoryEntity
import com.example.passionDaily.data.entity.QuoteEntity
import com.example.passionDaily.data.entity.TermsConsentEntity
import com.example.passionDaily.data.entity.UserEntity
import com.example.passionDaily.data.relation.FavoriteWithQuotes
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PassionDailyRepository
    @Inject
    constructor(
        private val userDao: UserDao,
        private val quoteDao: QuoteDao,
        private val categoryDao: QuoteCategoryDao,
        private val favoriteDao: FavoriteDao,
        private val notificationDao: NotificationDao,
        private val termsConsentDao: TermsConsentDao,
    ) {
        // Quotes
        fun getAllQuotes(): Flow<List<QuoteEntity>> = quoteDao.getAllQuotes()

        fun getQuoteById(quoteId: Int): Flow<QuoteEntity?> = quoteDao.getQuoteById(quoteId)

        fun getQuotesByCategory(categoryId: Int): Flow<List<QuoteEntity>> = quoteDao.getQuotesByCategory(categoryId)

        // Categories
        fun getAllCategories(): List<QuoteCategoryEntity> = categoryDao.getAllCategories()

        fun getCategoriesWithQuotes() = categoryDao.getCategoriesWithQuotes()

        // Users
        suspend fun getUserById(userId: Int): Flow<UserEntity?> = userDao.getUserById(userId)

        suspend fun insertUser(user: UserEntity) = userDao.insertUser(user)

        fun getUsersWithFavorites() = userDao.getUsersWithFavorites()

        fun getUsersWithFavoritesAndQuotes() = userDao.getUsersWithFavoritesAndQuotes()

        fun getUsersWithTermsConsents() = userDao.getUsersWithTermsConsents()

        fun getUsersWithNotifications() = userDao.getUsersWithNotifications()

        // Favorites
        fun getFavoritesByUserId(userId: Int): Flow<List<FavoriteEntity>> = favoriteDao.getFavoritesByUserId(userId)

        fun getFavoritesWithQuotesByUserId(userId: Int): Flow<List<FavoriteWithQuotes>> = favoriteDao.getFavoritesWithQuotesByUserId(userId)

        suspend fun addFavorite(favorite: FavoriteEntity) = favoriteDao.addFavorite(favorite)

        suspend fun deleteFavorite(favorite: FavoriteEntity) = favoriteDao.deleteFavorite(favorite)

        // Notifications
        suspend fun getNotificationSettings(userId: Int): NotificationEntity? = notificationDao.getNotificationSettings(userId)

        suspend fun insertNotificationSettings(settings: NotificationEntity) = notificationDao.insertNotificationSettings(settings)

        suspend fun updateNotificationSettings(settings: NotificationEntity) = notificationDao.updateNotificationSettings(settings)

        // Terms Consent
        fun getTermsConsentsByUserId(userId: Int): Flow<List<TermsConsentEntity>> = termsConsentDao.getTermsConsentsByUserId(userId)

        suspend fun insertTermsConsent(termsConsent: TermsConsentEntity) = termsConsentDao.insertTermsConsent(termsConsent)
    }
