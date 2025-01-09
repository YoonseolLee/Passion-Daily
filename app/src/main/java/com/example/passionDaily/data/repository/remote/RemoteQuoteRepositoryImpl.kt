package com.example.passionDaily.data.repository.remote

import android.util.Log
import com.example.passionDaily.data.local.dao.FavoriteDao
import com.example.passionDaily.data.local.dao.QuoteCategoryDao
import com.example.passionDaily.data.local.dao.QuoteDao
import com.example.passionDaily.data.remote.model.FavoriteQuote
import com.example.passionDaily.data.remote.model.Quote
import com.example.passionDaily.data.repository.local.LocalFavoriteRepository
import com.example.passionDaily.data.repository.local.LocalFavoriteRepositoryImpl
import com.example.passionDaily.util.QuoteCategory
import com.example.passionDaily.util.QuoteConstants
import com.example.passionDaily.util.QuoteConstants.DEFAULT_TIMESTAMP
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RemoteQuoteRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : RemoteQuoteRepository{

    data class QuoteResult(
        val quotes: List<Quote>,
        val lastDocument: DocumentSnapshot?
    )

    override suspend fun getQuotesByCategory(
        category: QuoteCategory,
        pageSize: Int,
        lastLoadedQuote: DocumentSnapshot?
    ): QuoteResult = withContext(Dispatchers.IO) {
        try {
            val query = buildCategoryQuery(category, pageSize, lastLoadedQuote)
            val result = query.get().await()
            result.toQuoteResult()
        } catch (e: Exception) {
            Log.e("getQuotesByCategory", "Error fetching quotes: ${e.message}")
            throw e
        }
    }

    private fun buildCategoryQuery(
        category: QuoteCategory,
        pageSize: Int,
        lastLoadedQuote: DocumentSnapshot?
    ): Query {
        val categoryStr = category.getLowercaseCategoryId()

        try {
            val baseQuery = firestore.collection("categories")
                .document(categoryStr)
                .collection("quotes")

            val orderedQuery = baseQuery.orderBy("createdAt")
            val paginatedQuery = lastLoadedQuote?.let {
                orderedQuery.startAfter(it)
            } ?: orderedQuery

            val finalQuery = paginatedQuery.limit(pageSize.toLong())

            return finalQuery
        } catch (e: Exception) {
            Log.e("RemoteQuoteRepository", "Error in buildCategoryQuery", e)
            throw e
        }
    }

    private fun QuerySnapshot.toQuoteResult(): QuoteResult {
        Log.d("toQuoteResult", "toQuoteResult 진입")
        return if (isEmpty) {
            QuoteResult(emptyList(), null)
        } else {
            QuoteResult(
                quotes = documents.map { it.toQuote() },
                lastDocument = documents.lastOrNull()
            )
        }
    }

    private fun DocumentSnapshot.toQuote(): Quote {
        Log.d("toQuote", "toQuote 진입")
        return Quote(
            id = id,
            category = QuoteCategory.fromEnglishName(getString("category") ?: "") ?: QuoteCategory.OTHER,
            text = getString("text") ?: "",
            person = getString("person") ?: "",
            imageUrl = getString("imageUrl") ?: "",
            createdAt = getString("createdAt") ?: DEFAULT_TIMESTAMP,
            modifiedAt = getString("modifiedAt") ?: DEFAULT_TIMESTAMP,
            shareCount = getLong("shareCount")?.toInt() ?: 0
        )
    }

    override suspend fun incrementShareCount(quoteId: String, category: QuoteCategory): Unit = withContext(Dispatchers.IO) {
        try {
            firestore.collection("categories")
                .document(category.getLowercaseCategoryId())
                .collection("quotes")
                .document(quoteId)
                .update("shareCount", FieldValue.increment(1))
                .await()
            Log.d("Repository", "Share count incremented successfully for quoteId: $quoteId")
        } catch (e: Exception) {
            Log.e("Repository", "Error incrementing share count for quoteId: $quoteId", e)
            throw e
        }
    }
}