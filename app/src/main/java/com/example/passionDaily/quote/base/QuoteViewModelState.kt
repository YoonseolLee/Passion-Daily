package com.example.passionDaily.quote.base

import com.example.passionDaily.data.remote.model.Quote
import com.example.passionDaily.util.QuoteCategory
import kotlinx.coroutines.flow.StateFlow

interface QuoteViewModelState {
    val quotes: StateFlow<List<Quote>>
    val currentQuote: StateFlow<Quote?>
    val isLoading: StateFlow<Boolean>
    val hasReachedEnd: StateFlow<Boolean>
    val selectedCategory: StateFlow<QuoteCategory>
    val currentQuoteIndex: StateFlow<Int>
}