package com.example.passionDaily.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.passionDaily.data.entity.NotificationEntity

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE user_id = :userId")
    suspend fun getNotificationSettings(userId: Int): NotificationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificationSettings(settings: NotificationEntity)

    @Update
    suspend fun updateNotificationSettings(settings: NotificationEntity)
}
