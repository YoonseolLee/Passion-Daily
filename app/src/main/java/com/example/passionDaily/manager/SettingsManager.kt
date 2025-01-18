package com.example.passionDaily.manager

import com.example.passionDaily.data.repository.local.LocalFavoriteRepository
import com.example.passionDaily.data.repository.local.LocalQuoteCategoryRepository
import com.example.passionDaily.data.repository.local.LocalQuoteRepository
import com.example.passionDaily.data.repository.local.LocalUserRepository
import com.example.passionDaily.data.repository.remote.RemoteUserRepository
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalTime
import javax.inject.Inject

class SettingsManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val remoteUserRepository: RemoteUserRepository,
    private val localUserRepository: LocalUserRepository,
    private val localFavoriteRepository: LocalFavoriteRepository,
    private val localQuoteRepository: LocalQuoteRepository,
    private val localQuoteCategoryRepository: LocalQuoteCategoryRepository
) {

    suspend fun loadUserSettings(
        userId: String,
        onSettingsLoaded: suspend (notificationEnabled: Boolean, notificationTime: String?) -> Unit
    ) {
        localUserRepository.getUserById(userId)?.let { user ->
            onSettingsLoaded(user.notificationEnabled, user.notificationTime)
        }
    }

    suspend fun updateNotificationSettings(userId: String, enabled: Boolean) {
        remoteUserRepository.updateNotificationSettingsToFirestore(userId, enabled)
        localUserRepository.updateNotificationSettingsToRoom(userId, enabled)
    }

    suspend fun updateNotificationTime(userId: String, time: LocalTime) {
        remoteUserRepository.updateNotificationTimeToFirestore(userId, time.toString())
        localUserRepository.updateNotificationTimeToRoom(userId, time.toString())
    }

    suspend fun deleteUserData(userId: String) {
        remoteUserRepository.deleteUserDataFromFirestore(userId)
        deleteUserDataFromRoom(userId)
    }

    private suspend fun deleteUserDataFromRoom(userId: String) {
        localUserRepository.deleteUser(userId)
        localFavoriteRepository.deleteAllFavoritesByUserId(userId)
        localQuoteRepository.deleteAllQuotes()
        localQuoteCategoryRepository.deleteAllCategories()
    }
}