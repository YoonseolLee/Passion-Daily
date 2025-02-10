package com.example.passionDaily.favorites.manager

import com.example.passionDaily.favorites.usecase.RemoveFavoritesUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject

class FavoritesRemoveManagerImpl @Inject constructor(
    private val removeFavoritesUseCase: RemoveFavoritesUseCase
) : FavoritesRemoveManager {

    override suspend fun getRequiredDataForRemove(
        firebaseAuth: FirebaseAuth,
        categoryId: Int
    ): Pair<FirebaseUser, Int>? {
        return removeFavoritesUseCase.getRequiredDataForRemove(firebaseAuth, categoryId)
    }

    override suspend fun deleteLocalFavorite(userId: String, quoteId: String, categoryId: Int) {
        removeFavoritesUseCase.deleteLocalFavorite(userId, quoteId, categoryId)
    }

    override suspend fun deleteFavoriteFromFirestore(
        currentUser: FirebaseUser,
        quoteId: String,
        categoryId: Int
    ) {
        removeFavoritesUseCase.deleteFavoriteFromFirestore(currentUser, quoteId, categoryId)
    }
}
