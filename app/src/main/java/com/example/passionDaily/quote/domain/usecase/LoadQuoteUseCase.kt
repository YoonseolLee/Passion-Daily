package com.example.passionDaily.quote.domain.usecase

import android.util.Log
import com.example.passionDaily.constants.ViewModelConstants.Quote.PAGE_SIZE
import com.example.passionDaily.constants.ViewModelConstants.Quote.TAG
import com.example.passionDaily.data.remote.model.Quote
import com.example.passionDaily.quote.data.remote.RemoteQuoteRepository
import com.example.passionDaily.quote.domain.model.QuoteResult
import com.example.passionDaily.quote.stateholder.QuoteStateHolder
import com.example.passionDaily.util.QuoteCategory
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class LoadQuoteUseCase @Inject constructor(
    private val remoteQuoteRepository: RemoteQuoteRepository,
    private val quoteStateHolder: QuoteStateHolder,
) {
    suspend fun loadQuotesBeforeTarget(
        quoteId: String,
        category: QuoteCategory
    ): List<Quote> {
        return try {
            remoteQuoteRepository.getQuotesBeforeId(
                category = category,
                targetQuoteId = quoteId,
                limit = PAGE_SIZE
            ).also { quotes ->
                Log.d(TAG, "Quotes before target: ${quotes.map { it.id }}")
            }
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "Failed to load quotes before target", e)
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error while loading quotes before target", e)
            emptyList()
        }
    }

    suspend fun loadTargetQuote(
        quoteId: String,
        category: QuoteCategory
    ): Quote? {
        return try {
            remoteQuoteRepository.getQuoteById(quoteId, category)
                ?.also { quote ->
                    Log.d(TAG, "Target quote: ${quote.id}")
                } ?: run {
                Log.w(TAG, "Target quote not found: $quoteId")
                null
            }
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "Failed to load target quote", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error while loading target quote", e)
            null
        }
    }

    suspend fun replaceQuotes(beforeQuotes: List<Quote>, targetQuote: Quote) {
        clearExistingQuotes()
        addInitialQuotes(beforeQuotes, targetQuote)
    }

    private suspend fun clearExistingQuotes() {
        quoteStateHolder.clearQuotes()
    }

    private suspend fun addInitialQuotes(beforeQuotes: List<Quote>, targetQuote: Quote) {
        val allQuotes = beforeQuotes + targetQuote
        quoteStateHolder.addQuotes(allQuotes, true)
    }

    suspend fun loadFurtherQuotes(
        quoteId: String,
        category: QuoteCategory
    ): QuoteResult {
        val afterQuotesResult = loadQuotesAfterTarget(quoteId, category)
        addAfterQuotes(afterQuotesResult)
        return afterQuotesResult
    }

    private suspend fun loadQuotesAfterTarget(
        quoteId: String,
        category: QuoteCategory
    ): QuoteResult {
        return try {
            remoteQuoteRepository.getQuotesAfterId(
                category = category,
                afterQuoteId = quoteId,
                limit = PAGE_SIZE
            )
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "Failed to load quotes after target", e)
            QuoteResult(emptyList(), null)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error while loading quotes after target", e)
            QuoteResult(emptyList(), null)
        }
    }

    private suspend fun addAfterQuotes(afterQuotesResult: QuoteResult) {
        if (afterQuotesResult.quotes.isNotEmpty()) {
            quoteStateHolder.addQuotes(afterQuotesResult.quotes, false)
        }
    }

    fun getUpdatedLastLoadedQuote(document: DocumentSnapshot?): DocumentSnapshot? {
        return document
    }

    fun getUpdatedQuoteIndex(index: Int): Int {
        return index
    }

    fun shouldLoadMoreQuotes(
        nextIndex: Int,
        currentQuotes: List<Quote>,
        hasQuoteReachedEnd: StateFlow<Boolean>
    ): Boolean {
        return nextIndex >= currentQuotes.size && !hasQuoteReachedEnd.value
    }

    fun isLastQuote(nextIndex: Int, currentQuotes: List<Quote>): Boolean {
        return nextIndex >= currentQuotes.size
    }

    fun shouldLoadMoreQuotesIfNeeded(
        selectedCategory: QuoteCategory?,
        isQuoteLoading: Boolean,
        lastLoadedQuote: DocumentSnapshot?
    ): Boolean {
        return selectedCategory != null && !isQuoteLoading && lastLoadedQuote != null
    }

    suspend fun loadQuotesAfter(
        category: QuoteCategory,
        lastQuoteId: String,
        pageSize: Int
    ): QuoteResult {
        return remoteQuoteRepository.getQuotesAfterId(
            category = category,
            afterQuoteId = lastQuoteId,
            limit = pageSize
        )
    }

    suspend fun updateQuotesAfterLoading(
        result: QuoteResult,
        lastLoadedQuote: (DocumentSnapshot?) -> Unit
    ) {
        if (result.quotes.isEmpty()) {
            quoteStateHolder.updateHasQuoteReachedEnd(true)
        } else {
            quoteStateHolder.addQuotes(result.quotes, false)
            lastLoadedQuote(result.lastDocument)
        }
    }
}