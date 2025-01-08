package com.example.passionDaily.data.local.dao

import android.util.Log
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.passionDaily.data.local.dto.FavoriteWithCategory
import com.example.passionDaily.data.local.entity.FavoriteEntity
import com.example.passionDaily.data.local.entity.QuoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("SELECT * FROM favorites WHERE user_id = :userId AND quote_id = :quoteId AND category_id = :categoryId")
    fun checkFavoriteEntity(
        userId: String,
        quoteId: String,
        categoryId: Int
    ): Flow<FavoriteEntity?>

    @Query("DELETE FROM favorites WHERE user_id = :userId AND quote_id = :quoteId AND category_id = :categoryId")
    suspend fun deleteFavorite(
        userId: String,
        quoteId: String,
        categoryId: Int
    )

    @Query("DELETE FROM favorites WHERE user_id = :userId")
    suspend fun deleteAllFavoritesByUserId(userId: String)

    @Query("SELECT  quote_id FROM favorites WHERE user_id = :userId")
    fun getAllFavoriteIds(userId: String): Flow<List<String>>

    @Query(
        """
            SELECT f.quote_id, q.category_id 
            FROM favorites f 
            INNER JOIN quotes q ON f.quote_id = q.quote_id 
            WHERE f.user_id = :userId
        """
    )
    fun getAllFavoriteIdsWithCategory(userId: String):
            Flow<List<FavoriteWithCategory>>

    @Query("SELECT * FROM favorites")
    suspend fun getAllFavorites(): List<FavoriteEntity>

    @Query("SELECT * FROM quotes")
    suspend fun getAllQuotes(): List<QuoteEntity>

}
