package com.example.passionDaily.user.data.local.dao

import androidx.room.Dao

import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.passionDaily.user.data.local.entity.UserEntity

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Transaction
    suspend fun upsertUser(user: UserEntity) {
        val existingUser = getUserByUserId(user.userId)
        if (existingUser != null) {
            updateUser(user)
        } else {
            insertUser(user)
        }
    }

    @Query("DELETE FROM user WHERE user_id = :userId")
    suspend fun deleteUser(userId: String)

    @Query("SELECT * FROM user")
    suspend fun getAllUsers(): List<UserEntity>

    @Query("SELECT * FROM user WHERE user_id = :userId")
    suspend fun getUserByUserId(userId: String): UserEntity?

    @Query("SELECT * FROM user WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM user WHERE notification_enabled = :enabled")
    suspend fun getUsersWithNotifications(enabled: Boolean): List<UserEntity>

    @Query("DELETE FROM user WHERE user_id = :userId")
    suspend fun deleteUserById(userId: String)

    @Query("UPDATE user SET notification_enabled = :enabled WHERE user_id = :userId")
    suspend fun updateNotificationSetting(userId: String, enabled: Boolean)

    @Query("UPDATE user SET notification_time = :time WHERE user_id = :userId")
    suspend fun updateNotificationTime(userId: String, time: String)
}