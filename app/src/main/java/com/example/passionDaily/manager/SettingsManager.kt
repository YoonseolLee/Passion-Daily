package com.example.passionDaily.manager

import android.util.Log
import com.example.passionDaily.data.repository.local.LocalFavoriteRepository
import com.example.passionDaily.data.repository.local.LocalQuoteCategoryRepository
import com.example.passionDaily.data.repository.local.LocalQuoteRepository
import com.example.passionDaily.data.repository.local.LocalUserRepository
import com.example.passionDaily.data.repository.remote.RemoteUserRepository
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class SettingsManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val remoteUserRepository: RemoteUserRepository,
    private val localUserRepository: LocalUserRepository,
    private val localFavoriteRepository: LocalFavoriteRepository,
    private val localQuoteRepository: LocalQuoteRepository,
    private val localQuoteCategoryRepository: LocalQuoteCategoryRepository
) {

    companion object {
        private const val TAG = "SettingsManager"
    }

    suspend fun loadUserSettings(
        userId: String,
        onSettingsLoaded: suspend (notificationEnabled: Boolean, notificationTime: String?) -> Unit
    ) {
        localUserRepository.getUserById(userId)?.let { user ->
            onSettingsLoaded(user.notificationEnabled, user.notificationTime)
        }
    }

    suspend fun updateNotificationSettings(userId: String, enabled: Boolean) {
        Log.d(TAG, "Updating notification settings: enabled=$enabled for user=$userId")

        try {
            remoteUserRepository.updateNotificationSettingsToFirestore(userId, enabled)
            Log.d(TAG, "Successfully updated notification settings in Firestore")

            localUserRepository.updateNotificationSettingsToRoom(userId, enabled)
            Log.d(TAG, "Successfully updated notification settings in Room")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update notification settings", e)
            throw e
        }
    }

    suspend fun updateNotificationTime(userId: String, time: LocalTime) {
        Log.d(TAG, "Updating notification time to ${time.hour}:${time.minute} for user=$userId")

        try {
            val timeString = time.format(DateTimeFormatter.ofPattern("HH:mm"))
            remoteUserRepository.updateNotificationTimeToFirestore(userId, timeString)
            Log.d(TAG, "Successfully updated notification time in Firestore")

            localUserRepository.updateNotificationTimeToRoom(userId, timeString)
            Log.d(TAG, "Successfully updated notification time in Room")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update notification time", e)
            throw e
        }
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