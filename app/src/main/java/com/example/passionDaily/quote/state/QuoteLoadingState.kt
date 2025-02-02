package com.example.passionDaily.quote.state

import com.example.passionDaily.quote.base.QuoteViewModelState
import com.example.passionDaily.util.QuoteCategory
import kotlinx.coroutines.flow.StateFlow

interface QuoteLoadingState : QuoteViewModelState  {
    override val isLoading: StateFlow<Boolean>
    override val hasReachedEnd: StateFlow<Boolean>
    override val selectedCategory: StateFlow<QuoteCategory>
    override val currentQuoteIndex: StateFlow<Int>
}