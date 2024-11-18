package com.example.passionDaily.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.passionDaily.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE user_id = :userId")
    fun getNotificationSettings(userId: Int): Flow<NotificationEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificationSettings(notifications: NotificationEntity)

    @Update
    suspend fun updateNotificationSettings(notifications: NotificationEntity)

    @Delete
    suspend fun deleteNotificationSettings(notifications: NotificationEntity)
}
