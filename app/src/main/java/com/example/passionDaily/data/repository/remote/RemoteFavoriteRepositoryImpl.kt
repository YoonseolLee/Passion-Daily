package com.example.passionDaily.data.repository.remote

import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RemoteFavoriteRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : RemoteFavoriteRepository {

    override suspend fun addFavoriteToFirestore(
        currentUser: FirebaseUser,
        quoteId: String,
        favoriteData: HashMap<String, String?>,
    ): Unit = withContext(Dispatchers.IO) {

        try {
            firestore.collection("favorites")
                .document(currentUser.uid)
                .set(hashMapOf<String, Any>(), SetOptions.merge())

            firestore.collection("favorites")
                .document(currentUser.uid)
                .collection("saved_quotes")
                .document(quoteId)
                .set(favoriteData)
        } catch (e: Exception) {
            Log.e("Firestore", "Firestore 즐겨찾기 추가 실패", e)
            throw e
        }
    }

    override suspend fun deleteFavoriteFromFirestore(
        currentUser: FirebaseUser,
        quoteId: String,
    ): Unit = withContext(Dispatchers.IO) {

        try {
            firestore.collection("favorites")
                .document(currentUser.uid)
                .collection("saved_quotes")
                .document(quoteId)
                .delete()
        } catch (e: Exception) {
            Log.e("Firestore", "Firestore 즐겨찾기 삭제 실패", e)
            throw e
        }
    }
}