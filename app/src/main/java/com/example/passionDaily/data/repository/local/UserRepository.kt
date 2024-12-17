package com.example.passionDaily.data.repository.local

import com.example.passionDaily.data.local.dao.UserDao
import com.example.passionDaily.data.local.entity.UserEntity
import com.example.passionDaily.data.local.relation.UserWithFavorites
import com.example.passionDaily.data.local.relation.UserWithNotification
import com.example.passionDaily.data.local.relation.UserWithTermsConsent
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val firestore: FirebaseFirestore
) {

    suspend fun insertUser(user: UserEntity) {
        return userDao.insertUser(user)
    }

    suspend fun updateUser(user: UserEntity) {
        userDao.updateUser(user)
    }

    suspend fun deleteUser(user: UserEntity) {
        userDao.deleteUser(user)
    }

    suspend fun deleteUserById(userId: String) {
        userDao.deleteUserById(userId)
    }

    suspend fun getAllUsers(): List<UserEntity> {
        return userDao.getAllUsers()
    }

    suspend fun getUserByUserId(userId: String): UserEntity? {
        return userDao.getUserByUserId(userId)
    }

    suspend fun getUserByEmail(email: String): UserEntity? {
        return userDao.getUserByEmail(email)
    }

    suspend fun getUsersByAccountStatus(isDeleted: Boolean): List<UserEntity> {
        return userDao.getUsersByAccountStatus(isDeleted)
    }

    suspend fun getUsersWithNotificationEnabled(): List<UserEntity> {
        return userDao.getUsersWithNotificationsEnabled()
    }

    suspend fun getUserWithTermsConsent(userId: String): UserWithTermsConsent? {
        return userDao.getUserWithTermsConsent(userId)
    }

    suspend fun getUserWithNotification(userId: String): UserWithNotification? {
        return userDao.getUserWithNotification(userId)
    }

    suspend fun getUserWithFavorites(userId: String): UserWithFavorites? {
        return userDao.getUserWithFavorites(userId)
    }
}