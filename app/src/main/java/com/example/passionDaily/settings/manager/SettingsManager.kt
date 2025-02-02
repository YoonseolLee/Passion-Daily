package com.example.passionDaily.settings.manager

import android.util.Log
import com.example.passionDaily.data.repository.local.LocalFavoriteRepository
import com.example.passionDaily.data.repository.local.LocalQuoteCategoryRepository
import com.example.passionDaily.quote.data.local.LocalQuoteRepository
import com.example.passionDaily.data.repository.local.LocalUserRepository
import com.example.passionDaily.data.repository.remote.RemoteUserRepository
import com.example.passionDaily.settings.usecase.LoadUserInfoUseCase
import com.example.passionDaily.settings.usecase.ParseTimeUseCase
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class SettingsManager @Inject constructor(
    private val remoteUserRepository: RemoteUserRepository,
    private val localUserRepository: LocalUserRepository,
    private val localFavoriteRepository: LocalFavoriteRepository,
    private val localQuoteRepository: LocalQuoteRepository,
    private val localQuoteCategoryRepository: LocalQuoteCategoryRepository,
    private val loadUserInfoUseCase: LoadUserInfoUseCase,
    private val parseTimeUseCase: ParseTimeUseCase,
) {

    companion object {
        private const val TAG = "SettingsManager"
    }

    suspend fun loadUserSettings(
        userId: String,
        onSettingsLoaded: suspend (notificationEnabled: Boolean, notificationTime: String?) -> Unit
    ) {
        loadUserInfoUseCase.loadUserSettings(userId, onSettingsLoaded)
    }

    fun parseTime(timeStr: String): LocalTime {
        return parseTimeUseCase.parseTime(timeStr)
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