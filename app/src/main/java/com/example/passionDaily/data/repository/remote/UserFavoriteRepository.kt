package com.example.passionDaily.data.repository.remote

import com.example.passionDaily.data.remote.model.UserFavorite
import com.google.firebase.firestore.FirebaseFirestore

class UserFavoriteRepository(private val db: FirebaseFirestore) {
    companion object {
        private const val COLLECTION_USER_FAVORITES = "user_favorites"
        private const val SUBCOLLECTION_QUOTES = "quotes"
    }

    suspend fun addFavorite(userId: String, favorite: UserFavorite) {
        db.collection(COLLECTION_USER_FAVORITES)
            .document(userId)
            .collection(SUBCOLLECTION_QUOTES)
            .document(favorite.id)
            .set(favorite)
    }

    suspend fun removeFavorite(userId: String, quoteId: String) {
        db.collection(COLLECTION_USER_FAVORITES)
            .document(userId)
            .collection(SUBCOLLECTION_QUOTES)
            .document(quoteId)
            .delete()
    }
}