package com.example.passionDaily.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.passionDaily.data.entity.FavoriteEntity
import com.example.passionDaily.data.relation.FavoriteWithQuotes
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites WHERE user_id = :userId")
    fun getFavoritesByUserId(userId: Int): Flow<List<FavoriteEntity>>

    @Query(
        """
        SELECT * FROM favorites f 
        INNER JOIN quotes q ON f.quote_id = q.quote_id 
        WHERE f.user_id = :userId
    """,
    )
    fun getFavoritesWithQuotesByUserId(userId: Int): Flow<List<FavoriteWithQuotes>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Delete
    suspend fun deleteFavorite(favorite: FavoriteEntity)
}
