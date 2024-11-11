package com.example.passionDaily.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.passionDaily.data.entity.FavoriteEntity
import com.example.passionDaily.data.relation.FavoriteWithQuotes
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites WHERE userId = :userId")
    fun getFavoritesByUserId(userId: Int): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Delete
    suspend fun deleteFavorite(favorite: FavoriteEntity)

    @Transaction
    @Query("SELECT * FROM favorites")
    fun getFavoritesWithQuotes(): List<FavoriteWithQuotes>
}
