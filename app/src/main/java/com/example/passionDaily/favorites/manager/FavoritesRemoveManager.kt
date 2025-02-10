package com.example.passionDaily.favorites.manager

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

interface FavoritesRemoveManager {

    suspend fun getRequiredDataForRemove(
        firebaseAuth: FirebaseAuth,
        categoryId: Int
    ): Pair<FirebaseUser, Int>?

    suspend fun deleteLocalFavorite(userId: String, quoteId: String, categoryId: Int)

    suspend fun deleteFavoriteFromFirestore(
        currentUser: FirebaseUser,
        quoteId: String,
        categoryId: Int
    )
}