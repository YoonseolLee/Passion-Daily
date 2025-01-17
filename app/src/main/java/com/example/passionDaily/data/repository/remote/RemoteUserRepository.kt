package com.example.passionDaily.data.repository.remote

import com.example.passionDaily.data.remote.model.User

interface RemoteUserRepository {

    suspend fun isUserRegistered(userId: String): Boolean
    suspend fun updateLastSyncDate(userId: String)
    suspend fun fetchFirestoreUser(userId: String): User
    suspend fun syncFirestoreUserToRoom(userId: String)
}