package com.example.passionDaily.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.passionDaily.favorites.data.local.dao.FavoriteDao
import com.example.passionDaily.quotecategory.data.local.dao.QuoteCategoryDao
import com.example.passionDaily.quote.data.local.dao.QuoteDao
import com.example.passionDaily.favorites.data.local.entity.FavoriteEntity
import com.example.passionDaily.quotecategory.data.local.entity.QuoteCategoryEntity
import com.example.passionDaily.quote.data.local.entity.QuoteEntity

@Database(
    entities = [
        UserEntity::class,
        QuoteEntity::class,
        QuoteCategoryEntity::class,
        FavoriteEntity::class,
    ],
    version = 1,
    exportSchema = false
)
abstract class PassionDailyDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun quoteDao(): QuoteDao
    abstract fun categoryDao(): QuoteCategoryDao
    abstract fun favoriteDao(): FavoriteDao
}
