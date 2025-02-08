package com.example.passionDaily.quote.manager

import com.example.passionDaily.constants.ViewModelConstants.Quote.PAGE_SIZE
import com.example.passionDaily.quote.data.remote.model.Quote
import com.example.passionDaily.quote.domain.model.QuoteResult
import com.example.passionDaily.quote.domain.usecase.QuoteListManagementUseCase
import com.example.passionDaily.quote.domain.usecase.QuoteLoadingUseCase
import com.example.passionDaily.quote.domain.usecase.QuoteStateManagementUseCase
import com.example.passionDaily.quotecategory.model.QuoteCategory
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class QuoteLoadingManagerImpl @Inject constructor(
    private val stateManagementUseCase: QuoteStateManagementUseCase,
    private val loadingUseCase: QuoteLoadingUseCase,
    private val listManagementUseCase: QuoteListManagementUseCase,
) : QuoteLoadingManager {
    override suspend fun startQuoteLoading() {
        stateManagementUseCase.updateIsQuoteLoading(true)
    }

    override suspend fun fetchQuotesByCategory(
        category: QuoteCategory,
        pageSize: Int,
        lastLoadedQuote: DocumentSnapshot?
    ): QuoteResult {
        return loadingUseCase.fetchQuotesByCategory(category, pageSize, lastLoadedQuote)
    }

    override suspend fun setHasQuoteReachedEndTrue() {
        stateManagementUseCase.updateHasQuoteReachedEnd(true)
    }

    override suspend fun addQuotesToState(quotes: List<Quote>, isNewCategory: Boolean) {
        stateManagementUseCase.addQuotes(quotes, isNewCategory)
    }

    override suspend fun loadQuotesBeforeTarget(
        quoteId: String,
        category: QuoteCategory
    ): List<Quote> {
        return loadingUseCase.loadQuotesBeforeTarget(quoteId, category, PAGE_SIZE)
    }

    override suspend fun loadTargetQuote(
        quoteId: String,
        category: QuoteCategory
    ): Quote? = withContext(Dispatchers.IO) {
        loadingUseCase.loadTargetQuote(quoteId, category)
    }

    override suspend fun replaceQuotes(beforeQuotes: List<Quote>, targetQuote: Quote) {
        listManagementUseCase.clearExistingQuotes()
        listManagementUseCase.addInitialQuotes(beforeQuotes, targetQuote)
    }

    override suspend fun loadFurtherQuotes(
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
        return loadingUseCase.loadQuotesAfterTarget(quoteId, category, PAGE_SIZE)
    }

    private suspend fun addAfterQuotes(afterQuotesResult: QuoteResult) {
        listManagementUseCase.addAfterQuotes(afterQuotesResult)
    }

    override fun getUpdatedLastLoadedQuote(document: DocumentSnapshot?): DocumentSnapshot? {
        return listManagementUseCase.getUpdatedLastLoadedQuote(document)
    }

    override fun getUpdatedQuoteIndex(index: Int): Int {
        return loadingUseCase.getUpdatedQuoteIndex(index)
    }

    override fun shouldLoadMoreQuotes(
        nextIndex: Int,
        currentQuotes: List<Quote>,
        hasQuoteReachedEnd: StateFlow<Boolean>
    ): Boolean {
        return loadingUseCase.shouldLoadMoreQuotes(nextIndex, currentQuotes, hasQuoteReachedEnd)
    }

    override fun isLastQuote(nextIndex: Int, currentQuotes: List<Quote>): Boolean {
        return loadingUseCase.isLastQuote(nextIndex, currentQuotes)
    }

    override suspend fun loadQuotesAfter(
        category: QuoteCategory,
        lastQuoteId: String,
        pageSize: Int
    ): QuoteResult {
        return loadingUseCase.loadQuotesAfter(category, lastQuoteId, pageSize)
    }

    override suspend fun updateQuotesAfterLoading(
        result: QuoteResult,
        lastLoadedQuote: (DocumentSnapshot?) -> Unit
    ) {
        loadingUseCase.updateQuotesAfterLoading(result, lastLoadedQuote)
    }

    override suspend fun clearQuotes() {
        listManagementUseCase.clearExistingQuotes()
    }

    override suspend fun updateSelectedCategory(category: QuoteCategory) {
        listManagementUseCase.updateSelectedCategory(category)
    }
}