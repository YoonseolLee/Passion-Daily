package com.example.passionDaily.quote.manager

import com.example.passionDaily.data.remote.model.Quote
import com.example.passionDaily.quote.domain.model.QuoteResult
import com.example.passionDaily.util.QuoteCategory
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.StateFlow

interface QuoteLoadingManager {
    suspend fun startQuoteLoading()
    suspend fun fetchQuotesByCategory(
        category: QuoteCategory,
        pageSize: Int,
        lastLoadedQuote: DocumentSnapshot?
    ): QuoteResult

    suspend fun setHasQuoteReachedEndTrue()
    suspend fun addQuotesToState(quotes: List<Quote>, isNewCategory: Boolean)
    suspend fun loadQuotesBeforeTarget(quoteId: String, category: QuoteCategory): List<Quote>
    suspend fun loadTargetQuote(quoteId: String, category: QuoteCategory): Quote?
    suspend fun replaceQuotes(beforeQuotes: List<Quote>, targetQuote: Quote)
    suspend fun loadFurtherQuotes(quoteId: String, category: QuoteCategory): QuoteResult
    suspend fun clearQuotes()
    suspend fun updateSelectedCategory(category: QuoteCategory?)

    fun getUpdatedLastLoadedQuote(document: DocumentSnapshot?): DocumentSnapshot?
    fun getUpdatedQuoteIndex(index: Int): Int
    fun shouldLoadMoreQuotes(
        nextIndex: Int,
        currentQuotes: List<Quote>,
        hasQuoteReachedEnd: StateFlow<Boolean>
    ): Boolean

    fun isLastQuote(nextIndex: Int, currentQuotes: List<Quote>): Boolean

    suspend fun loadQuotesAfter(
        category: QuoteCategory,
        lastQuoteId: String,
        pageSize: Int
    ): QuoteResult

    suspend fun updateQuotesAfterLoading(
        result: QuoteResult,
        lastLoadedQuote: (DocumentSnapshot?) -> Unit
    )
}