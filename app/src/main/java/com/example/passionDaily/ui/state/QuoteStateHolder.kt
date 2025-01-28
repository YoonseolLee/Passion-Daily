package com.example.passionDaily.ui.state

import com.example.passionDaily.data.remote.model.Quote
import com.example.passionDaily.util.QuoteCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuoteStateHolder @Inject constructor() {
    private val _selectedQuoteCategory = MutableStateFlow<QuoteCategory?>(QuoteCategory.EFFORT)
    val selectedQuoteCategory: StateFlow<QuoteCategory?> = _selectedQuoteCategory.asStateFlow()

    private val _quotes = MutableStateFlow<List<Quote>>(emptyList())
    val quotes: StateFlow<List<Quote>> = _quotes.asStateFlow()

    private val _hasReachedEnd = MutableStateFlow(false)
    val hasReachedEnd: StateFlow<Boolean> = _hasReachedEnd.asStateFlow()

    private val _categories = MutableStateFlow(QuoteCategory.values().map { it.koreanName })
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _isQuoteLoading = MutableStateFlow(false)
    val isQuoteLoading: StateFlow<Boolean> = _isQuoteLoading.asStateFlow()

    private val _hasQuoteReachedEnd = MutableStateFlow(false)
    val hasQuoteReachedEnd: StateFlow<Boolean> = _hasQuoteReachedEnd.asStateFlow()

    private var _currentQuoteIndex: StateFlow<Int>? = null
    private var _currentQuote: StateFlow<Quote?>? = null

    suspend fun updateSelectedCategory(category: QuoteCategory?) {
        _selectedQuoteCategory.emit(category)
    }

    suspend fun updateQuotes(newQuotes: List<Quote>) {
        _quotes.emit(newQuotes)
    }

    suspend fun updateHasReachedEnd(hasReachedEnd: Boolean) {
        _hasReachedEnd.emit(hasReachedEnd)
    }

    suspend fun updateCategories(newCategories: List<String>) {
        _categories.emit(newCategories)
    }

    suspend fun updateIsQuoteLoading(isLoading: Boolean) {
        _isQuoteLoading.emit(isLoading)
    }

    suspend fun updateHasQuoteReachedEnd(hasReachedEnd: Boolean) {
        _hasQuoteReachedEnd.emit(hasReachedEnd)
    }

    suspend fun addQuotes(additionalQuotes: List<Quote>, isNewCategory: Boolean = false) {
        _quotes.emit(if (isNewCategory) {
            additionalQuotes  // 새 카테고리면 기존 목록을 새것으로 교체
        } else {
            _quotes.value + additionalQuotes  // 기존 목록에 새 명언들 추가
        })
    }

    suspend fun clearQuotes() {
        _quotes.emit(emptyList())
    }
}