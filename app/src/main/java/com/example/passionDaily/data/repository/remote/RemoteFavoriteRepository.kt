package com.example.passionDaily.data.repository.remote

import com.google.firebase.auth.FirebaseUser

interface RemoteFavoriteRepository {
    suspend fun addFavoriteToFirestore(
        currentUser: FirebaseUser,
        quoteId: String,
        favoriteData: HashMap<String, String?>,
    )

    suspend fun deleteFavoriteFromFirestore(
        currentUser: FirebaseUser,
        quoteId: String,
    )
}