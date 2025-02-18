package com.example.passionDaily.user.data.local.repository

import com.example.passionDaily.user.data.local.dao.UserDao
import com.example.passionDaily.user.data.local.entity.UserEntity
import com.example.passionDaily.user.data.remote.model.User
import com.example.passionDaily.util.TimeUtil
import javax.inject.Inject

class LocalUserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val timeUtil: TimeUtil
) : LocalUserRepository {

    override suspend fun saveUser(userEntity: UserEntity) {
        userDao.upsertUser(userEntity)
    }

    override fun convertToUserEntity(firestoreUser: User): UserEntity {
        return UserEntity(
            userId = firestoreUser.id,
            name = firestoreUser.name,
            notificationEnabled = firestoreUser.notificationEnabled,
            notificationTime = firestoreUser.notificationTime,
            lastSyncDate = timeUtil.parseTimestamp(firestoreUser.lastSyncDate)
        )
    }

    override suspend fun updateNotificationSettingsToRoom(userId: String, enabled: Boolean) {
        userDao.updateNotificationSetting(userId, enabled)
    }

    override suspend fun getUserById(userId: String): UserEntity? {
        return userDao.getUserByUserId(userId)
    }

    override suspend fun updateNotificationTimeToRoom(userId: String, newTime: String) {
        userDao.updateNotificationTime(userId, newTime)
    }

    override suspend fun deleteUser(userId: String) {
        userDao.deleteUser(userId)
    }
}
