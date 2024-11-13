package com.example.passionDaily.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.passionDaily.data.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites WHERE user_id = :userId")
    fun getFavoritesByUserId(userId: Int): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE user_id = :userId AND quote_id = :quoteId)")
    suspend fun isFavorite(
        userId: Int,
        quoteId: Int,
    ): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Update
    suspend fun updateFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE user_id = :userId AND quote_id = :quoteId")
    suspend fun deleteFavorite(
        userId: Int,
        quoteId: Int,
    )
}
