package com.example.passionDaily.settings.manager

import android.util.Log
import com.example.passionDaily.favorites.data.local.repository.LocalFavoriteRepository
import com.example.passionDaily.quotecategory.data.local.repository.LocalQuoteCategoryRepository
import com.example.passionDaily.quote.data.local.repository.LocalQuoteRepository
import com.example.passionDaily.user.data.local.repository.LocalUserRepository
import com.example.passionDaily.user.data.remote.repository.RemoteUserRepository
import com.example.passionDaily.settings.domain.usecase.LoadUserInfoUseCase
import javax.inject.Inject

class UserSettingsManagerImpl @Inject constructor(
    private val remoteUserRepository: RemoteUserRepository,
    private val localUserRepository: LocalUserRepository,
    private val localFavoriteRepository: LocalFavoriteRepository,
    private val localQuoteRepository: LocalQuoteRepository,
    private val localQuoteCategoryRepository: LocalQuoteCategoryRepository,
    private val loadUserInfoUseCase: LoadUserInfoUseCase
) : UserSettingsManager {
    override suspend fun loadUserSettings(
        userId: String,
        onSettingsLoaded: suspend (notificationEnabled: Boolean, notificationTime: String?) -> Unit
    ) {
        loadUserInfoUseCase.loadUserSettings(userId, onSettingsLoaded)
    }

    override suspend fun deleteUserData(userId: String) {
        remoteUserRepository.deleteUserDataFromFirestore(userId)
        deleteUserDataFromRoom(userId)
    }

    private suspend fun deleteUserDataFromRoom(userId: String) {
        Log.d("deleteUserDataFromRoom", "Starting to delete favorites for user: $userId")
        // 1. 해당 사용자의 즐겨찾기만 삭제
        localFavoriteRepository.deleteAllFavoritesByUserId(userId)
        Log.d("deleteUserDataFromRoom", "Successfully deleted favorites")

        Log.d("deleteUserDataFromRoom", "Starting to delete user")
        // 2. 사용자 정보만 삭제
        localUserRepository.deleteUser(userId)
        Log.d("deleteUserDataFromRoom", "Successfully deleted user")
    }
}
