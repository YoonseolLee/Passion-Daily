package com.example.passionDaily.quote.domain.usecase

import com.example.passionDaily.data.remote.model.Quote
import com.example.passionDaily.quote.stateholder.QuoteStateHolder
import javax.inject.Inject

class QuoteStateManagementUseCase @Inject constructor(
    private val quoteStateHolder: QuoteStateHolder
) {
    suspend fun updateIsQuoteLoading(isLoading: Boolean) {
        quoteStateHolder.updateIsQuoteLoading(isLoading)
    }

    suspend fun updateHasQuoteReachedEnd(hasReachedEnd: Boolean) {
        quoteStateHolder.updateHasQuoteReachedEnd(hasReachedEnd)
    }

    suspend fun addQuotes(quotes: List<Quote>, isNewCategory: Boolean) {
        quoteStateHolder.addQuotes(quotes, isNewCategory)
    }
}