package com.example.passionDaily.quote.stateholder

import android.util.Log
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
        Log.d("StateHolder", "updateIsQuoteLoading: $isLoading, time=${System.currentTimeMillis()}")
        _isQuoteLoading.emit(isLoading)
    }

    override suspend fun updateHasQuoteReachedEnd(hasReachedEnd: Boolean) {
        _hasQuoteReachedEnd.emit(hasReachedEnd)
    }

    override suspend fun addQuotes(additionalQuotes: List<Quote>, isNewCategory: Boolean) {
        Log.d("StateHolder", "addQuotes: count=${additionalQuotes.size}, isNew=$isNewCategory, time=${System.currentTimeMillis()}")

        // 1. 비어있는 리스트가 들어오면 처리하지 않음
        if (additionalQuotes.isEmpty()) return

        val newQuotes = if (isNewCategory) {
            // 2. 현재 값과 새 값이 동일하면 업데이트하지 않음
            if (additionalQuotes == _quotes.value) return
            additionalQuotes
        } else {
            // 3. 추가되는 경우에도 이미 포함되어 있으면 업데이트하지 않음
            if (_quotes.value.containsAll(additionalQuotes)) return
            _quotes.value + additionalQuotes
        }

        _quotes.emit(newQuotes)
    }

    override suspend fun clearQuotes() {
        _quotes.emit(emptyList())
    }
}