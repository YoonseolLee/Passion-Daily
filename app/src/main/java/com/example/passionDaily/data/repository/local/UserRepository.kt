package com.example.passionDaily.data.repository.local

import com.example.passionDaily.data.local.dao.UserDao
import com.example.passionDaily.data.local.entity.UserEntity
import com.example.passionDaily.data.local.relation.UserWithFavorites
import com.example.passionDaily.data.local.relation.UserWithNotification
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userDao: UserDao,
) {

    suspend fun getUserWithFavorites(userId: Int): UserWithFavorites? {
        return userDao.getUserWithFavorites(userId)
    }

    suspend fun getUserById(userId: Int): UserEntity? {
        return userDao.getUserById(userId)
    }

    suspend fun getUserByEmail(email: String): UserEntity? {
        return userDao.getUserByEmail(email)
    }

    suspend fun insertUser(user: UserEntity): Long {
        return userDao.insertUser(user)
    }

    suspend fun updateUser(user: UserEntity) {
        userDao.updateUser(user)
    }

    suspend fun deleteUser(user: UserEntity) {
        userDao.deleteUser(user)
    }

    suspend fun getUserWithNotification(userId: Int): UserWithNotification? {
        return userDao.getUserWithNotification(userId)
    }
}