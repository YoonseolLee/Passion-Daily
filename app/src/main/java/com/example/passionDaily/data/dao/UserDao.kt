package com.example.passionDaily.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.passionDaily.data.entity.UserEntity
import com.example.passionDaily.data.relation.UserWithFavorites
import com.example.passionDaily.data.relation.UserWithFavoritesAndQuotes
import com.example.passionDaily.data.relation.UserWithSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE userId = :userId")
    suspend fun getUserById(userId: Int): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Transaction
    @Query("SELECT * FROM users")
    fun getUsersWithSettings(): List<UserWithSettings>

    @Transaction
    @Query("SELECT * FROM users")
    fun getUsersWithFavorites(): List<UserWithFavorites>

    @Transaction
    @Query("SELECT * FROM users")
    fun getUsersWithFavoritesAndQuotes(): List<UserWithFavoritesAndQuotes>
}
