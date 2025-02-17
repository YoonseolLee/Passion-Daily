package com.example.passionDaily.favorites.usecase

import androidx.room.Transaction
import com.example.passionDaily.favorites.data.local.entity.FavoriteEntity
import com.example.passionDaily.favorites.data.local.repository.LocalFavoriteRepository
import com.example.passionDaily.favorites.data.remote.repository.RemoteFavoriteRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RemoveFavoritesUseCase @Inject constructor(
    private val localFavoriteRepository: LocalFavoriteRepository,
    private val remoteFavoriteRepository: RemoteFavoriteRepository
) {

    suspend fun getRequiredDataForRemove(
        firebaseAuth: FirebaseAuth,
        categoryId: Int
    ): Pair<FirebaseUser, Int>? = withContext(Dispatchers.IO) {
        val currentUser = firebaseAuth.currentUser ?: run {
            return@withContext null
        }
        Pair(currentUser, categoryId)
    }

    @Transaction
    suspend fun deleteLocalFavorite(userId: String, quoteId: String, categoryId: Int) =
        withContext(Dispatchers.IO) {
            deleteFavorite(userId, quoteId, categoryId)

            getRemainingFavorites(quoteId, categoryId)
        }

    private suspend fun deleteFavorite(userId: String, quoteId: String, categoryId: Int) {
        localFavoriteRepository.deleteFavorite(userId, quoteId, categoryId)
    }

    private suspend fun getRemainingFavorites(
        quoteId: String,
        categoryId: Int
    ): List<FavoriteEntity> {
        return localFavoriteRepository.getFavoritesForQuote(quoteId, categoryId)
    }

    suspend fun deleteFavoriteFromFirestore(
        currentUser: FirebaseUser,
        quoteId: String,
        categoryId: Int
    ) {
        remoteFavoriteRepository.deleteFavoriteFromFirestore(currentUser, quoteId, categoryId)
    }
}