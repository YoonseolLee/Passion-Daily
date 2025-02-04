package com.example.passionDaily.settings.usecase

import com.example.passionDaily.user.data.local.repository.LocalUserRepository
import javax.inject.Inject

class LoadUserInfoUseCase @Inject constructor(
    private val localUserRepository: LocalUserRepository
) {
    suspend fun loadUserSettings(
        userId: String,
        onSettingsLoaded: suspend (notificationEnabled: Boolean, notificationTime: String?) -> Unit
    ) {
        localUserRepository.getUserById(userId)?.let { user ->
            onSettingsLoaded(user.notificationEnabled, user.notificationTime)
        }
    }
}