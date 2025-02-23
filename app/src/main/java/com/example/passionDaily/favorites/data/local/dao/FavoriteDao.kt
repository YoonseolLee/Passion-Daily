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
        """
    )
    fun getAllFavorites(): Flow<List<QuoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("SELECT * FROM favorites WHERE quote_id = :quoteId AND category_id = :categoryId")
    fun checkFavoriteEntity(
        quoteId: String,
        categoryId: Int
    ): Flow<FavoriteEntity?>

    @Query("DELETE FROM favorites WHERE quote_id = :quoteId AND category_id = :categoryId")
    suspend fun deleteFavorite(
        quoteId: String,
        categoryId: Int
    )

    @Query("SELECT * FROM favorites WHERE quote_id = :quoteId AND category_id = :categoryId")
    suspend fun getFavoritesForQuote(quoteId: String, categoryId: Int): List<FavoriteEntity>
}
