package com.example.passionDaily.user.data.local.repository

import com.example.passionDaily.user.data.local.entity.UserEntity
import com.example.passionDaily.user.data.remote.model.User

interface LocalUserRepository {

    suspend fun saveUser(userEntity: UserEntity)
    fun convertToUserEntity(firestoreUser: User): UserEntity
    suspend fun updateNotificationSettingsToRoom(userId: String, enabled: Boolean)
    suspend fun getUserById(userId: String): UserEntity?
    suspend fun updateNotificationTimeToRoom(userId: String, newTime: String)
    suspend fun deleteUser(userId: String)
}