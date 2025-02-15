package com.example.passionDaily.settings.domain.usecase

import com.example.passionDaily.user.data.local.repository.LocalUserRepository
import com.example.passionDaily.user.data.remote.repository.RemoteUserRepository
import javax.inject.Inject

class UpdateNotificationUseCase @Inject constructor(
    private val remoteUserRepository: RemoteUserRepository,
    private val localUserRepository: LocalUserRepository
) {
    suspend fun updateNotificationSettingsToFirestore(userId: String, enabled: Boolean) {
        remoteUserRepository.updateNotificationSettingsToFirestore(userId, enabled)
    }

    suspend fun updateNotificationSettingsToRoom(userId: String, enabled: Boolean) {
        localUserRepository.updateNotificationSettingsToRoom(userId, enabled)
    }
}