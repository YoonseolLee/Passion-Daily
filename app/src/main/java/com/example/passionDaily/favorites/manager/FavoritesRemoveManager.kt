package com.example.passionDaily.favorites.manager

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

interface FavoritesRemoveManager {
    suspend fun deleteLocalFavorite(quoteId: String, categoryId: Int)
}