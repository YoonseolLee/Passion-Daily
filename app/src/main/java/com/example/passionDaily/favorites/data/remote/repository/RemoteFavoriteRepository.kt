package com.example.passionDaily.favorites.data.remote.repository

import com.google.firebase.auth.FirebaseUser

interface RemoteFavoriteRepository {
    suspend fun addFavoriteToFirestore(
        currentUser: FirebaseUser,
        documentId: String,
        favoriteData: HashMap<String, String>,
    )

    suspend fun deleteFavoriteFromFirestore(
        currentUser: FirebaseUser,
        quoteId: String,
        categoryId: Int
    )

    suspend fun getLastQuoteNumber(
        currentUser: FirebaseUser,
        category: String
    ) : Long
}