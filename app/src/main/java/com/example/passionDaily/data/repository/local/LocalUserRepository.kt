package com.example.passionDaily.data.repository.local

import com.example.passionDaily.data.local.entity.UserEntity
import com.example.passionDaily.data.remote.model.User

interface LocalUserRepository {

    suspend fun saveUser(userEntity: UserEntity)
    fun convertToUserEntity(firestoreUser: User): UserEntity

}