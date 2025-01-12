package com.example.passionDaily.data.repository.remote

import android.util.Log
import com.example.passionDaily.data.remote.model.Quote
import com.example.passionDaily.util.QuoteCategory
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
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
            firestore.collection("favorites")
                .document(currentUser.uid)
                .collection("saved_quotes")
                .document(documentId)  // 예: "love_quote_000001", "business_quote_000001"
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
            Log.e("Firestore", "마지막 quote 번호 가져오기 실패", e)
            throw e
        }
    }

    private suspend fun fetchFavoriteQuotesInfo(userId: String): List<Pair<String, String>> {
        return try {
            val savedQuotesSnapshot = firestore.collection("favorites")
                .document(userId)
                .collection("saved_quotes")
                .get()
                .await()

            savedQuotesSnapshot.documents.mapNotNull { document ->
                val category = document.getString("category")
                val quoteId = document.getString("quote_id")
                if (category != null && quoteId != null) {
                    Pair(category, quoteId)
                } else {
                    Log.e("Repository", "Invalid saved quote document: ${document.id}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("Repository", "Error fetching favorite quotes info", e)
            emptyList()
        }
    }

    private suspend fun fetchQuotesFromFavorites(favoriteQuotesInfo: List<Pair<String, String>>): List<Quote> {
        return favoriteQuotesInfo.mapNotNull { (category, quoteId) ->
            try {
                val document = firestore.collection("categories")
                    .document(category)
                    .collection("quotes")
                    .document(quoteId)
                    .get()
                    .await()

                if (document.exists()) {
                    mapDocumentToQuote(document)
                } else {
                    Log.e("Repository", "Quote document doesn't exist: $quoteId")
                    null
                }
            } catch (e: Exception) {
                Log.e("Repository", "Error fetching quote $quoteId: ${e.message}")
                null
            }
        }
    }

    private fun mapDocumentToQuote(document: DocumentSnapshot): Quote {
        return Quote(
            id = document.id,
            category = QuoteCategory.fromEnglishName(document.getString("category") ?: "") ?: QuoteCategory.OTHER,
            text = document.getString("text") ?: "",
            person = document.getString("person") ?: "",
            imageUrl = document.getString("imageUrl") ?: "",
            createdAt = document.getString("createdAt") ?: "1970-01-01 00:00",
            modifiedAt = document.getString("modifiedAt") ?: "1970-01-01 00:00",
            shareCount = document.getLong("shareCount")?.toInt() ?: 0
        )
    }
}