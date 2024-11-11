package com.example.passionDaily.di

import android.content.Context
import androidx.room.Room
import com.example.passionDaily.data.dao.FavoriteDao
import com.example.passionDaily.data.dao.NotificationDao
import com.example.passionDaily.data.dao.QuoteCategoryDao
import com.example.passionDaily.data.dao.QuoteDao
import com.example.passionDaily.data.dao.TermsConsentDao
import com.example.passionDaily.data.dao.UserDao
import com.example.passionDaily.data.database.PassionDailyDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): PassionDailyDatabase =
        Room
            .databaseBuilder(
                context,
                PassionDailyDatabase::class.java,
                "passion_daily.db",
            ).build()

    @Provides
    @Singleton
    fun provideUserDao(database: PassionDailyDatabase): UserDao = database.userDao()

    @Provides
    @Singleton
    fun provideQuoteDao(database: PassionDailyDatabase): QuoteDao = database.quoteDao()

    @Provides
    @Singleton
    fun provideCategoryDao(database: PassionDailyDatabase): QuoteCategoryDao = database.categoryDao()

    @Provides
    @Singleton
    fun provideFavoriteDao(database: PassionDailyDatabase): FavoriteDao = database.favoriteDao()

    @Provides
    @Singleton
    fun provideNotificationDao(database: PassionDailyDatabase): NotificationDao = database.notificationDao()

    @Provides
    @Singleton
    fun provideTermsConsentDao(database: PassionDailyDatabase): TermsConsentDao = database.termsConsentDao()
}
