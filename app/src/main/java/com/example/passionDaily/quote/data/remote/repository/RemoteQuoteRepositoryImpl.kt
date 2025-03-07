package com.example.passionDaily.quote.data.remote.repository

import com.example.passionDaily.R
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
import com.google.firebase.firestore.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
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
            val baseQuery =
                firestore.collection(stringProvider.getString(R.string.collection_categories))
                    .document(category.name.lowercase())
                    .collection(stringProvider.getString(R.string.collection_quotes))

            val query = baseQuery
                .orderBy(FieldPath.documentId())
                .endBefore(targetQuoteId)
                .get()
                .await()

            query.documents.map { it.toQuote() }
        } catch (e: FirebaseFirestoreException) {
            emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getQuotesAfterId(
        category: QuoteCategory,
        afterQuoteId: String,
        limit: Int
    ): QuoteResult = withContext(Dispatchers.IO) {
        try {
            val query =
                firestore.collection(stringProvider.getString(R.string.collection_categories))
                    .document(category.name.lowercase())
                    .collection(stringProvider.getString(R.string.collection_quotes))
                    .orderBy(FieldPath.documentId())
                    .startAfter(afterQuoteId)
                    .limit(limit.toLong())
                    .get()
                    .await()

            QuoteResult(
                quotes = query.documents.map { it.toQuote() },
                lastDocument = query.documents.lastOrNull()
            )
        } catch (e: Exception) {
            QuoteResult(emptyList(), null)
        }
    }

    override suspend fun getQuotesByCategory(
        category: QuoteCategory,
        pageSize: Int,
        lastLoadedQuote: DocumentSnapshot?
    ): QuoteResult = withContext(Dispatchers.IO) {
        try {
            withTimeout(stringProvider.getString(R.string.firestore_timeout_ms).toLong()) {
                val query = buildCategoryQuery(category, pageSize, lastLoadedQuote)

                // 먼저 캐시에서 시도
                val cacheResult = query.get(Source.CACHE).await()
                if (!cacheResult.isEmpty) {
                    return@withTimeout cacheResult.toQuoteResult()
                }

                // 캐시에 없으면 서버에서 가져옴
                val serverResult = query.get(Source.SERVER).await()
                serverResult.toQuoteResult()
            }
        } catch (e: Exception) {
            when {
                e is UnknownHostException ||
                        e.cause is UnknownHostException ||
                        e is TimeoutCancellationException ->
                    throw IOException(stringProvider.getString(R.string.error_network_firestore), e)

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
            val baseQuery =
                firestore.collection(stringProvider.getString(R.string.collection_categories))
                    .document(categoryStr)
                    .collection(stringProvider.getString(R.string.collection_quotes))

            val orderedQuery = baseQuery.orderBy(FieldPath.documentId())
            val paginatedQuery = lastLoadedQuote?.let {
                orderedQuery.startAfter(it)
            } ?: orderedQuery

            paginatedQuery.limit(pageSize.toLong())
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun incrementShareCount(
        quoteId: String,
        category: QuoteCategory
    ): Unit = withContext(Dispatchers.IO) {
        try {
            firestore.collection(stringProvider.getString(R.string.collection_categories))
                .document(category.getLowercaseCategoryId())
                .collection(stringProvider.getString(R.string.collection_quotes))
                .document(quoteId)
                .update(
                    stringProvider.getString(R.string.field_share_count),
                    FieldValue.increment(1)
                )
                .await()
        } catch (e: FirebaseFirestoreException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getQuoteById(
        quoteId: String,
        category: QuoteCategory
    ): Quote? = withContext(Dispatchers.IO) {
        try {
            firestore.collection(stringProvider.getString(R.string.collection_categories))
                .document(category.name.lowercase())
                .collection(stringProvider.getString(R.string.collection_quotes))
                .document(quoteId)
                .get()
                .await()
                .toQuote()
        } catch (e: FirebaseFirestoreException) {
            null
        } catch (e: Exception) {
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
            category = QuoteCategory.fromEnglishName(
                getString(stringProvider.getString(R.string.field_category)) ?: ""
            ) ?: QuoteCategory.OTHER,
            text = getString(stringProvider.getString(R.string.field_text)) ?: "",
            person = getString(stringProvider.getString(R.string.field_person)) ?: "",
            imageUrl = getString(stringProvider.getString(R.string.field_image_url)) ?: "",
            createdAt = getString(stringProvider.getString(R.string.field_created_at))
                ?: stringProvider.getString(R.string.default_timestamp),
            modifiedAt = getString(stringProvider.getString(R.string.field_modified_at))
                ?: stringProvider.getString(R.string.default_timestamp),
            shareCount = getLong(stringProvider.getString(R.string.field_share_count))?.toInt() ?: 0
        )
    }
}