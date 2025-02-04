package com.example.passionDaily.login.domain.usecase

import com.example.passionDaily.user.data.local.repository.LocalUserRepository
import com.example.passionDaily.user.data.remote.repository.RemoteUserRepository
import com.example.passionDaily.login.stateholder.AuthStateHolder
import com.example.passionDaily.util.mapper.UserProfileMapper
import javax.inject.Inject

class SaveUserProfileUseCase @Inject constructor(
    private val userProfileMapper: UserProfileMapper,
    private val localUserRepository: LocalUserRepository,
    private val remoteUserRepository: RemoteUserRepository,
    private val authStateHolder: AuthStateHolder
) {
    suspend fun saveToRoom(profileMap: Map<String, Any?>, userId: String): String {
        val userEntity = userProfileMapper.mapToUserEntity(profileMap)
        localUserRepository.saveUser(userEntity)  // Room 저장 실패시 예외는 자동으로 전파됨
        return userId
    }

    suspend fun saveUserToFirestore(userId: String, profileMap: Map<String, Any?>): String {
        remoteUserRepository.addUserProfile(userId, profileMap)
        return userId
    }

    suspend fun syncExistingUser(userId: String) {
        remoteUserRepository.updateLastSyncDate(userId)
        remoteUserRepository.syncFirestoreUserToRoom(userId)
        authStateHolder.setAuthenticated(userId)
    }
}