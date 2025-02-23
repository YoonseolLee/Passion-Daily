package com.example.passionDaily.favorites.manager

import com.example.passionDaily.favorites.usecase.RemoveFavoritesUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject

class FavoritesRemoveManagerImpl @Inject constructor(
    private val removeFavoritesUseCase: RemoveFavoritesUseCase
) : FavoritesRemoveManager {

    override suspend fun deleteLocalFavorite(quoteId: String, categoryId: Int) {
        removeFavoritesUseCase.deleteLocalFavorite(quoteId, categoryId)
    }
}
