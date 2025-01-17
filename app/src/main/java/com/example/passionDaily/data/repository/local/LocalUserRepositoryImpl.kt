package com.example.passionDaily.data.repository.local

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import com.example.passionDaily.data.local.dao.UserDao
import com.example.passionDaily.data.local.entity.UserEntity
import com.example.passionDaily.data.remote.model.User
import com.example.passionDaily.util.TimeUtil
import javax.inject.Inject

class LocalUserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val timeUtil: TimeUtil
) : LocalUserRepository {

    override suspend fun saveUser(userEntity: UserEntity) {
        try {
            userDao.insertUser(userEntity)
        } catch (e: SQLiteConstraintException) {
            Log.e("LocalUserRepository", "Constraint violation while saving user: ${e.message}", e)
            throw e
        } catch (e: Exception) {
            Log.e("LocalUserRepository", "Unexpected error while saving user: ${e.message}", e)
            throw e
        }
    }

    override fun convertToUserEntity(firestoreUser: User): UserEntity {
        return try {
            UserEntity(
                userId = firestoreUser.id,
                email = firestoreUser.email,
                notificationEnabled = firestoreUser.notificationEnabled,
                notificationTime = firestoreUser.notificationTime,
                lastSyncDate = timeUtil.parseTimestamp(firestoreUser.lastSyncDate)
            )
        } catch (e: Exception) {
            Log.e("LocalUserRepository", "Error converting Firestore user to UserEntity: ${e.message}", e)
            throw e
        }
    }
}