package com.example.passionDaily.data.local.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.passionDaily.data.local.entity.FavoriteEntity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorites ORDER BY added_at DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Delete
    suspend fun deleteFavorite(favorite: FavoriteEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE quote_id = :quoteId)")
    fun isQuoteFavorite(quoteId: String): Flow<Boolean>

    @Query("DELETE FROM favorites WHERE user_id = :userId")
    suspend fun deleteAllFavoritesByUserId(userId: String)

    @Query("SELECT quote_id FROM favorites WHERE user_id = :userId")
    fun getAllFavoriteIds(
        userId: String = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    ): Flow<List<String>>

    @Query(
        """
            SELECT f.quote_id, q.category_id 
            FROM favorites f 
            INNER JOIN quotes q ON f.quote_id = q.quote_id 
            WHERE f.user_id = :userId
        """
    )
    fun getAllFavoriteIdsWithCategory(
        userId: String = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    ): Flow<List<FavoriteWithCategory>>

    data class FavoriteWithCategory(
        @ColumnInfo(name = "quote_id")
        val quoteId: String,
        @ColumnInfo(name = "category_id")
        val categoryId: Int
    )
}
