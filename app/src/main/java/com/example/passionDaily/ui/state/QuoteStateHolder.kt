package com.example.passionDaily.ui.state

import com.example.passionDaily.data.remote.model.Quote
import com.example.passionDaily.util.QuoteCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuoteStateHolder @Inject constructor() {
    private val _selectedQuoteCategory = MutableStateFlow<QuoteCategory?>(QuoteCategory.EFFORT)
    val selectedQuoteCategory: StateFlow<QuoteCategory?> = _selectedQuoteCategory.asStateFlow()

    private val _quotes = MutableStateFlow<List<Quote>>(emptyList())
    val quotes: StateFlow<List<Quote>> = _quotes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _hasReachedEnd = MutableStateFlow(false)
    val hasReachedEnd: StateFlow<Boolean> = _hasReachedEnd.asStateFlow()

    fun startLoading() {
        _isLoading.value = true
    }

    fun stopLoading() {
        _isLoading.value = false
    }

    fun setHasReachedEnd(reached: Boolean) {
        _hasReachedEnd.value = reached
    }

    fun updateQuotes(newQuotes: List<Quote>) {
        _quotes.value = newQuotes
    }

    fun addQuotes(additionalQuotes: List<Quote>, isNewCategory: Boolean = false) {
        _quotes.value = if (isNewCategory) {
            additionalQuotes
        } else {
            _quotes.value + additionalQuotes
        }
    }

    fun updateSelectedCategory(category: QuoteCategory?) {
        _selectedQuoteCategory.value = category
    }

    fun clearQuotes() {
        _quotes.value = emptyList()
    }

    fun reset() {
        _hasReachedEnd.value = false
        _isLoading.value = false
        clearQuotes()
    }
}