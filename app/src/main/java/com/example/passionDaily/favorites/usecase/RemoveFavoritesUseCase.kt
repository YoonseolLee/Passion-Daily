package com.example.passionDaily.favorites.usecase

import androidx.room.Transaction
import com.example.passionDaily.favorites.data.local.entity.FavoriteEntity
import com.example.passionDaily.favorites.data.local.repository.LocalFavoriteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RemoveFavoritesUseCase @Inject constructor(
    private val localFavoriteRepository: LocalFavoriteRepository,
) {
    @Transaction
    suspend fun deleteLocalFavorite(quoteId: String, categoryId: Int) =
        withContext(Dispatchers.IO) {
            deleteFavorite(quoteId, categoryId)
            getRemainingFavorites(quoteId, categoryId)
        }

    private suspend fun deleteFavorite(quoteId: String, categoryId: Int) {
        localFavoriteRepository.deleteFavorite(quoteId, categoryId)
    }

    private suspend fun getRemainingFavorites(
        quoteId: String,
        categoryId: Int
    ): List<FavoriteEntity> {
        return localFavoriteRepository.getFavoritesForQuote(quoteId, categoryId)
    }
}