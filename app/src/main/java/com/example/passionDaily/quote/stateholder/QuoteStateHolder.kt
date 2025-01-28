package com.example.passionDaily.quote.stateholder

import com.example.passionDaily.data.remote.model.Quote
import com.example.passionDaily.util.QuoteCategory
import kotlinx.coroutines.flow.StateFlow

interface QuoteStateHolder {

    val selectedQuoteCategory: StateFlow<QuoteCategory?>
    val quotes: StateFlow<List<Quote>>
    val categories: StateFlow<List<String>>
    val isQuoteLoading: StateFlow<Boolean>
    val hasQuoteReachedEnd: StateFlow<Boolean>

    suspend fun updateSelectedCategory(category: QuoteCategory?)
    suspend fun updateIsQuoteLoading(isLoading: Boolean)
    suspend fun updateHasQuoteReachedEnd(hasReachedEnd: Boolean)
    suspend fun addQuotes(additionalQuotes: List<Quote>, isNewCategory: Boolean)
    suspend fun clearQuotes()
}