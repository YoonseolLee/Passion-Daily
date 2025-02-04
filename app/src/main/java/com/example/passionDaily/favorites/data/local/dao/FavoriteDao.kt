package com.example.passionDaily.favorites.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.passionDaily.favorites.data.local.dto.FavoriteWithCategory
import com.example.passionDaily.favorites.data.local.entity.FavoriteEntity
import com.example.passionDaily.quote.data.local.entity.QuoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Transaction
    @Query(
        """
    SELECT q.* 
    FROM quotes q 
    INNER JOIN favorites f 
    ON q.quote_id = f.quote_id 
    AND q.category_id = f.category_id  
    WHERE f.user_id = :userId
        """
    )
    fun getAllFavorites(userId: String): Flow<List<QuoteEntity>>

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

    @Query("SELECT * FROM favorites WHERE quote_id = :quoteId AND category_id = :categoryId")
    suspend fun getFavoritesForQuote(quoteId: String, categoryId: Int): List<FavoriteEntity>
}
