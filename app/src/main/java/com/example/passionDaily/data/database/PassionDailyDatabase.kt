package com.example.passionDaily.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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

@Database(
    entities = [
        UserEntity::class,
        QuoteEntity::class,
        QuoteCategoryEntity::class,
        FavoriteEntity::class,
        UserSettingsEntity::class,
    ],
    version = 1,
)
@TypeConverters(TypeConverters::class)
abstract class PassionDailyDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    abstract fun quoteDao(): QuoteDao

    abstract fun categoryDao(): QuoteCategoryDao

    abstract fun favoriteDao(): FavoriteDao

    abstract fun userSettingsDao(): UserSettingsDao
}
