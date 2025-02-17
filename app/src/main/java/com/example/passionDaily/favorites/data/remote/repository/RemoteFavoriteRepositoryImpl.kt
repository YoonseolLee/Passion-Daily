package com.example.passionDaily.favorites.data.remote.repository

import com.example.passionDaily.quotecategory.model.QuoteCategory
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.IOException
import java.net.UnknownHostException
import javax.inject.Inject

class RemoteFavoriteRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : RemoteFavoriteRepository {

    override suspend fun addFavoriteToFirestore(
        currentUser: FirebaseUser,
        documentId: String,
        favoriteData: HashMap<String, String>,
    ): Unit = withContext(Dispatchers.IO) {
        try {
            // 먼저 문서 존재 여부 확인
            withTimeout(3000L) {
                val docRef = firestore.collection("favorites")
                    .document(currentUser.uid)
                    .collection("saved_quotes")
                    .document(documentId)
                    .get()
                    .await()  // 여기서 네트워크 에러가 발생하면 바로 UnknownHostException

                firestore.collection("favorites")
                    .document(currentUser.uid)
                    .collection("saved_quotes")
                    .document(documentId)
                    .set(favoriteData)
                    .await()

            }
        } catch (e: Exception) {
            when {
                e is UnknownHostException ||
                        e.cause is UnknownHostException ||
                        e is TimeoutCancellationException ->
                    throw IOException("Network error while accessing Firestore", e)

                else -> throw e
            }
        }
    }

    override suspend fun deleteFavoriteFromFirestore(
        currentUser: FirebaseUser,
        quoteId: String,
        categoryId: Int,
    ): Unit = withContext(Dispatchers.IO) {
        try {
            val categoryEnglishName = QuoteCategory.fromCategoryId(categoryId)?.name?.lowercase()
                ?: throw IllegalArgumentException("Invalid category ID: $categoryId")

            withTimeout(3000L) {
                val quotesRef = firestore.collection("favorites")
                    .document(currentUser.uid)
                    .collection("saved_quotes")
                    .whereEqualTo("quoteId", quoteId)
                    .whereEqualTo("category", categoryEnglishName)
                    .get()
                    .await()

                if (quotesRef.isEmpty) {
                    throw IOException("Unable to find document. This might be due to network issues.")
                }

                for (document in quotesRef.documents) {
                    firestore.collection("favorites")
                        .document(currentUser.uid)
                        .collection("saved_quotes")
                        .document(document.id)
                        .delete()
                        .await()
                }
            }
        } catch (e: Exception) {
            when {
                e is UnknownHostException ||
                        e.cause is UnknownHostException ||
                        e is TimeoutCancellationException ->
                    throw IOException("Network error while accessing Firestore", e)

                else -> throw e
            }
        }
    }

    override suspend fun getLastQuoteNumber(
        currentUser: FirebaseUser,
        category: String
    ): Long = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("favorites")
                .document(currentUser.uid)
                .collection("saved_quotes")
                .get()
                .await()

            if (snapshot.isEmpty) return@withContext 0L

            // 가장 큰 문서 ID 번호 찾기
            var maxNumber = 0L
            snapshot.documents.forEach { doc ->
                val number = doc.id.substring(6).toLong() // "quote_" 이후의 숫자 부분
                if (number > maxNumber) {
                    maxNumber = number
                }
            }

            return@withContext maxNumber
        } catch (e: Exception) {
            throw e
        }
    }
}