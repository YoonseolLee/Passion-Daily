package com.example.passionDaily.quote.stateholder

import com.example.passionDaily.quote.data.remote.model.Quote
import com.example.passionDaily.quotecategory.model.QuoteCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuoteStateHolderImpl @Inject constructor() : QuoteStateHolder {
    private val _selectedQuoteCategory = MutableStateFlow<QuoteCategory>(QuoteCategory.EFFORT)
    override val selectedQuoteCategory: StateFlow<QuoteCategory> =
        _selectedQuoteCategory.asStateFlow()

    private val _quotes = MutableStateFlow<List<Quote>>(emptyList())
    override val quotes: StateFlow<List<Quote>> = _quotes.asStateFlow()

    private val _categories = MutableStateFlow(QuoteCategory.values().map { it.koreanName })
    override val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _isQuoteLoading = MutableStateFlow(false)
    override val isQuoteLoading: StateFlow<Boolean> = _isQuoteLoading.asStateFlow()

    private val _hasQuoteReachedEnd = MutableStateFlow(false)
    override val hasQuoteReachedEnd: StateFlow<Boolean> = _hasQuoteReachedEnd.asStateFlow()

    override suspend fun updateSelectedCategory(category: QuoteCategory) {
        _selectedQuoteCategory.emit(category)
    }

    override suspend fun updateIsQuoteLoading(isLoading: Boolean) {
        _isQuoteLoading.emit(isLoading)
    }

    override suspend fun updateHasQuoteReachedEnd(hasReachedEnd: Boolean) {
        _hasQuoteReachedEnd.emit(hasReachedEnd)
    }

    override suspend fun addQuotes(additionalQuotes: List<Quote>, isNewCategory: Boolean) {
        _quotes.emit(if (isNewCategory) {
            additionalQuotes  // 새 카테고리면 기존 목록을 새것으로 교체
        } else {
            _quotes.value + additionalQuotes  // 기존 목록에 새 명언들 추가
        })
    }

    override suspend fun clearQuotes() {
        _quotes.emit(emptyList())
    }
}