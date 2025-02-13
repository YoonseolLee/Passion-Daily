package com.example.passionDaily.quote.base

import com.example.passionDaily.login.state.AuthState
import com.example.passionDaily.quote.data.remote.model.Quote
import com.example.passionDaily.quotecategory.model.QuoteCategory
import kotlinx.coroutines.flow.StateFlow

interface QuoteViewModelState {
    val quotes: StateFlow<List<Quote>>
    val currentQuote: StateFlow<Quote?>
    val isLoading: StateFlow<Boolean>
    val hasReachedEnd: StateFlow<Boolean>
    val selectedCategory: StateFlow<QuoteCategory>
    val currentQuoteIndex: StateFlow<Int>
    val authState: StateFlow<AuthState>
}