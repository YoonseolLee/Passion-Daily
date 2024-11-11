package com.example.passionDaily.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import com.example.passionDaily.data.entity.FavoriteEntity
import com.example.passionDaily.data.relation.FavoriteWithQuotes
import com.example.passionDaily.data.relation.UserWithFavorites

@Dao
interface FavoriteDao {
    @Transaction
    @Query("SELECT * FROM favorites WHERE user_id = :userId")
    fun getFavoritesByUser(userId: Int): List<UserWithFavorites>

    @Transaction
    @Query(
        "SELECT f.*, q.* FROM favorites f " +
            "JOIN quotes q ON f.quote_id = q.quote_id " +
            "WHERE f.user_id = :userId",
    )
    @RewriteQueriesToDropUnusedColumns
    fun getFavoriteWithQuotes(userId: Int): List<FavoriteWithQuotes>

//    @Query(
//        """
//        SELECT * FROM favorites f
//        INNER JOIN quotes q ON f.quote_id = q.quote_id
//        WHERE f.user_id = :userId
//    """,
//    )
//    fun getFavoritesWithQuotesByUserId(userId: Int): Flow<List<FavoriteWithQuotes>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Delete
    suspend fun deleteFavorite(favorite: FavoriteEntity)
}
