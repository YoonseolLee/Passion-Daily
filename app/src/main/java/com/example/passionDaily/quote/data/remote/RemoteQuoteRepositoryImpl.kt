package com.example.passionDaily.quote.data.remote

import android.util.Log
import com.example.passionDaily.R
import com.example.passionDaily.constants.RepositoryConstants.Quote.TAG
import com.example.passionDaily.favorites.data.remote.repository.RemoteFavoriteRepositoryImpl
import com.example.passionDaily.favorites.data.remote.repository.RemoteFavoriteRepositoryImpl.Companion
import com.example.passionDaily.quote.data.remote.model.Quote
import com.example.passionDaily.quote.domain.model.QuoteResult
import com.example.passionDaily.resources.StringProvider
import com.example.passionDaily.quotecategory.model.QuoteCategory
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.time.withTimeout
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.IOException
import java.net.UnknownHostException
import javax.inject.Inject

class RemoteQuoteRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val stringProvider: StringProvider
) : RemoteQuoteRepository {

    override suspend fun getQuotesBeforeId(
        category: QuoteCategory,
        targetQuoteId: String,
        limit: Int
    ): List<Quote> = withContext(Dispatchers.IO) {
        try {
            val baseQuery = firestore.collection("categories")
                .document(category.name.lowercase())
                .collection("quotes")

            val query = baseQuery
                .orderBy(FieldPath.documentId())
                .endBefore(targetQuoteId)
                .get()
                .await()

            query.documents.map { it.toQuote() }
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "Firestore error in getQuotesBeforeId: ${e.message}", e)
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error in getQuotesBeforeId: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun getQuotesAfterId(
        category: QuoteCategory,
        afterQuoteId: String,
        limit: Int
    ): QuoteResult = withContext(Dispatchers.IO) {
        try {
            val query = firestore.collection("categories")
                .document(category.name.lowercase())
                .collection("quotes")
                .whereGreaterThan(FieldPath.documentId(), afterQuoteId)
                .orderBy(FieldPath.documentId())
                .limit(limit.toLong())
                .get()
                .await()

            QuoteResult(
                quotes = query.documents.map { it.toQuote() },
                lastDocument = query.documents.lastOrNull()
            )
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "Firestore error in getQuotesAfterId: ${e.message}", e)
            QuoteResult(emptyList(), null)
        } catch (e: Exception) {
            Log.e(TAG, "Error in getQuotesAfterId: ${e.message}", e)
            QuoteResult(emptyList(), null)
        }
    }

    override suspend fun getQuotesByCategory(
        category: QuoteCategory,
        pageSize: Int,
        lastLoadedQuote: DocumentSnapshot?
    ): QuoteResult = withContext(Dispatchers.IO) {
        try {
            withTimeout(3000L) {
                val query = buildCategoryQuery(category, pageSize, lastLoadedQuote)
                val result = query.get().await()
                result.toQuoteResult()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete favorite from Firestore", e)
            when {
                e is UnknownHostException ||
                        e.cause is UnknownHostException ||
                        e is TimeoutCancellationException ->
                    throw IOException("Network error while accessing Firestore", e)

                else -> throw e
            }
        }
    }

    private fun buildCategoryQuery(
        category: QuoteCategory,
        pageSize: Int,
        lastLoadedQuote: DocumentSnapshot?
    ): Query {
        val categoryStr = category.getLowercaseCategoryId()

        return try {
            val baseQuery = firestore.collection("categories")
                .document(categoryStr)
                .collection("quotes")

            val orderedQuery = baseQuery.orderBy("createdAt")
            val paginatedQuery = lastLoadedQuote?.let {
                orderedQuery.startAfter(it)
            } ?: orderedQuery

            paginatedQuery.limit(pageSize.toLong())
        } catch (e: Exception) {
            Log.e(TAG, "Error in buildCategoryQuery: ${e.message}", e)
            throw e
        }
    }

    override suspend fun incrementShareCount(
        quoteId: String,
        category: QuoteCategory
    ): Unit = withContext(Dispatchers.IO) {
        try {
            firestore.collection("categories")
                .document(category.getLowercaseCategoryId())
                .collection("quotes")
                .document(quoteId)
                .update("shareCount", FieldValue.increment(1))
                .await()
            Log.d(TAG, "Share count incremented for quote: $quoteId")
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "Firestore error in incrementShareCount: ${e.message}", e)
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error in incrementShareCount: ${e.message}", e)
            throw e
        }
    }

    override suspend fun getQuoteById(
        quoteId: String,
        category: QuoteCategory
    ): Quote? = withContext(Dispatchers.IO) {
        try {
            firestore.collection("categories")
                .document(category.name.lowercase())
                .collection("quotes")
                .document(quoteId)
                .get()
                .await()
                .toQuote()
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "Firestore error in getQuoteById: ${e.message}", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error in getQuoteById: ${e.message}", e)
            null
        }
    }

    private fun QuerySnapshot.toQuoteResult(): QuoteResult {
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
        return Quote(
            id = id,
            category = QuoteCategory.fromEnglishName(getString("category") ?: "")
                ?: QuoteCategory.OTHER,
            text = getString("text") ?: "",
            person = getString("person") ?: "",
            imageUrl = getString("imageUrl") ?: "",
            createdAt = getString("createdAt") ?: stringProvider.getString(R.string.default_timestamp),
            modifiedAt = getString("modifiedAt") ?: stringProvider.getString(R.string.default_timestamp),
            shareCount = getLong("shareCount")?.toInt() ?: 0
        )
    }
}