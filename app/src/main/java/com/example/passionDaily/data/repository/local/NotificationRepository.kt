package com.example.passionDaily.data.repository.local


import com.example.passionDaily.data.local.dao.NotificationDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NotificationRepository @Inject constructor(private val notificationDao: NotificationDao) {

    fun getNotificationSettings(userId: Int): Flow<NotificationEntity?> {
        return notificationDao.getNotificationSettings(userId)
    }

    suspend fun insertNotificationSettings(notifications: NotificationEntity) {
        notificationDao.insertNotificationSettings(notifications)
    }

    suspend fun updateNotificationSettings(notifications: NotificationEntity) {
        notificationDao.updateNotificationSettings(notifications)
    }

    suspend fun deleteNotificationSettings(notifications: NotificationEntity) {
        notificationDao.deleteNotificationSettings(notifications)
    }
}