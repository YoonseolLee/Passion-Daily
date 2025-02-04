package com.example.passionDaily.settings.usecase

import com.example.passionDaily.user.data.local.repository.LocalUserRepository
import com.example.passionDaily.user.data.remote.repository.RemoteUserRepository
import javax.inject.Inject

class UpdateNotificationUseCase @Inject constructor(
    private val remoteUserRepository: RemoteUserRepository,
    private val localUserRepository: LocalUserRepository
) {
    suspend fun updateNotificationSettings(userId: String, enabled: Boolean) {
        remoteUserRepository.updateNotificationSettingsToFirestore(userId, enabled)
        localUserRepository.updateNotificationSettingsToRoom(userId, enabled)
    }
}