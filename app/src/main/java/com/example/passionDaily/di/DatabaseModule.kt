package com.example.passionDaily.di

import android.content.Context
import androidx.room.Room
import com.example.passionDaily.favorites.data.local.dao.FavoriteDao
import com.example.passionDaily.quotecategory.data.local.dao.QuoteCategoryDao
import com.example.passionDaily.quote.data.local.dao.QuoteDao
import com.example.passionDaily.database.PassionDailyDatabase
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
}
