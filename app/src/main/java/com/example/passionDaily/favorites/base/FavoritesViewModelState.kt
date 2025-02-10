package com.example.passionDaily.favorites.base

import com.example.passionDaily.quote.data.local.entity.QuoteEntity
import com.example.passionDaily.quote.data.remote.model.Quote
import com.example.passionDaily.quotecategory.model.QuoteCategory
import kotlinx.coroutines.flow.StateFlow

interface FavoritesViewModelState {
    val favoriteQuotes: StateFlow<List<QuoteEntity>>
    val isFavoriteLoading: StateFlow<Boolean>
    val error: StateFlow<String?>
    val selectedQuoteCategory: StateFlow<QuoteCategory>
    val quotes: StateFlow<List<Quote>>
    val currentFavoriteQuote: StateFlow<QuoteEntity?>
}