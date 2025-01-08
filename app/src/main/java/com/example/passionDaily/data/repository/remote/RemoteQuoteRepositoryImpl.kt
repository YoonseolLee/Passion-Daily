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
    private val quoteDao: QuoteDao,
    private val localFavoriteRepository: LocalFavoriteRepository,
    private val remoteFavoriteRepository: RemoteFavoriteRepository,
    private val quoteCategoryDao: QuoteCategoryDao
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


    override suspend fun getFavoriteQuotes(): List<Quote> = withContext(Dispatchers.IO) {
        val currentUser = Firebase.auth.currentUser ?: return@withContext emptyList()

        try {
            // 1. 즐겨찾기 목록 가져오기
            val favoriteQuotesInfo = fetchFavoriteQuotesInfo(currentUser.uid)
            if (favoriteQuotesInfo.isEmpty()) return@withContext emptyList()

            // 2. 즐겨찾기 명언 목록 가져오기
            fetchQuotesFromFavorites(favoriteQuotesInfo)
        } catch (e: Exception) {
            Log.e("Repository", "Error fetching favorite quotes", e)
            emptyList()
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


    override suspend fun addToFavorites(quote: Quote, userId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun removeFromFavorites(quoteId: String, userId: String) {
        TODO("Not yet implemented")
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