package com.example.passionDaily.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.passionDaily.data.entity.UserEntity
import com.example.passionDaily.data.relation.UserWithFavorites
import com.example.passionDaily.data.relation.UserWithFavoritesAndQuotes
import com.example.passionDaily.data.relation.UserWithNotifications
import com.example.passionDaily.data.relation.UserWithTermsConsents
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE user_id = :userId")
    fun getUserById(userId: Int): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: UserEntity)

    @Transaction
    @Query("SELECT * FROM users")
    fun getUsersWithFavorites(): List<UserWithFavorites>

    @Transaction
    @Query("SELECT * FROM users")
    fun getUsersWithFavoritesAndQuotes(): List<UserWithFavoritesAndQuotes>

    @Transaction
    @Query("SELECT * FROM users")
    fun getUsersWithTermsConsents(): List<UserWithTermsConsents>

    @Transaction
    @Query("SELECT * FROM users")
    fun getUsersWithNotifications(): List<UserWithNotifications>
}
