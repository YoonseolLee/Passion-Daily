package com.example.passionDaily.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.passionDaily.data.local.entity.UserEntity
import com.example.passionDaily.data.local.relation.UserWithFavorites
import com.example.passionDaily.data.remote.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM user WHERE is_current_user = 1 LIMIT 1")
    fun getCurrentUser(): Flow<User?>

    @Transaction
    suspend fun setCurrentUser(userId: String) {
        // 모든 사용자의 isCurrentUser를 false로 설정
        clearCurrentUser()
        // 선택된 사용자만 true로 설정
        updateCurrentUser(userId, true)
    }

    @Query("UPDATE user SET is_current_user = 0")
    suspend fun clearCurrentUser()

    @Query("UPDATE user SET is_current_user = :isCurrent WHERE user_id = :userId")
    suspend fun updateCurrentUser(userId: String, isCurrent: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("DELETE FROM user WHERE user_id = :userId")
    suspend fun deleteUserById(userId: String)

    @Query("SELECT * FROM user")
    suspend fun getAllUsers(): List<UserEntity>

    @Query("SELECT * FROM user WHERE user_id = :userId LIMIT 1")
    suspend fun getUserByUserId(userId: String): UserEntity?

    @Query("SELECT * FROM user WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM user WHERE is_account_deleted = :isDeleted")
    suspend fun getUsersByAccountStatus(isDeleted: Boolean): List<UserEntity>

    @Query("SELECT * FROM user WHERE notification_enabled = 1")
    suspend fun getUsersWithNotificationsEnabled(): List<UserEntity>

    @Transaction
    @Query("SELECT * FROM user WHERE user_id = :userId LIMIT 1")
    suspend fun getUserWithFavorites(userId: String): UserWithFavorites?
}