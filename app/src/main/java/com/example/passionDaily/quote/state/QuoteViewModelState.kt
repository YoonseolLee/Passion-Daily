package com.example.passionDaily.quote.state

import com.example.passionDaily.data.remote.model.Quote
import com.example.passionDaily.util.QuoteCategory
import kotlinx.coroutines.flow.StateFlow

interface QuoteViewModelState {
    val currentQuote: StateFlow<Quote?>
    val isLoading: StateFlow<Boolean>
    val hasReachedEnd: StateFlow<Boolean>
    val selectedCategory: StateFlow<QuoteCategory?>
    val quotes: StateFlow<List<Quote>>
    val currentQuoteIndex: StateFlow<Int>
}