package com.example.passionDaily.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.passionDaily.data.local.entity.UserEntity
import com.example.passionDaily.data.local.relation.UserWithFavoriteQuotes
import com.example.passionDaily.data.local.relation.UserWithFavorites
import com.example.passionDaily.data.local.relation.UserWithNotification
import com.example.passionDaily.data.local.relation.UserWithTermsConsent

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE user_id = :userId")
    suspend fun getUserById(userId: Int): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Transaction
    @Query("SELECT * FROM users WHERE user_id = :userId")
    suspend fun getUserWithNotification(userId: Int): UserWithNotification?

    @Transaction
    @Query("SELECT * FROM users WHERE user_id = :userId")
    suspend fun getUserWithTermsConsent(userId: Int): UserWithTermsConsent?

    @Transaction
    @Query("SELECT * FROM users WHERE user_id = :userId")
    suspend fun getUserWithFavorites(userId: Int): UserWithFavorites?

    @Transaction
    @Query("SELECT * FROM users WHERE user_id = :userId")
    suspend fun getUserWithFavoriteQuotes(userId: Int): UserWithFavoriteQuotes?
}
