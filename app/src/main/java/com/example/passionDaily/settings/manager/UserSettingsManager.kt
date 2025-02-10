package com.example.passionDaily.settings.manager

interface UserSettingsManager {
    suspend fun loadUserSettings(
        userId: String,
        onSettingsLoaded: suspend (notificationEnabled: Boolean, notificationTime: String?) -> Unit
    )
    suspend fun deleteUserData(userId: String)
}
