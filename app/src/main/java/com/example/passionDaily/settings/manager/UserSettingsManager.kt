package com.example.passionDaily.settings.manager

import com.example.passionDaily.favorites.data.local.repository.LocalFavoriteRepository
import com.example.passionDaily.quotecategory.data.local.repository.LocalQuoteCategoryRepository
import com.example.passionDaily.quote.data.local.repository.LocalQuoteRepository
import com.example.passionDaily.user.data.local.repository.LocalUserRepository
import com.example.passionDaily.user.data.remote.repository.RemoteUserRepository
import com.example.passionDaily.settings.usecase.LoadUserInfoUseCase
import javax.inject.Inject

class UserSettingsManager @Inject constructor(
    private val remoteUserRepository: RemoteUserRepository,
    private val localUserRepository: LocalUserRepository,
    private val localFavoriteRepository: LocalFavoriteRepository,
    private val localQuoteRepository: LocalQuoteRepository,
    private val localQuoteCategoryRepository: LocalQuoteCategoryRepository,
    private val loadUserInfoUseCase: LoadUserInfoUseCase,
) {
    suspend fun loadUserSettings(
        userId: String,
        onSettingsLoaded: suspend (notificationEnabled: Boolean, notificationTime: String?) -> Unit
    ) {
        loadUserInfoUseCase.loadUserSettings(userId, onSettingsLoaded)
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