package com.example.passionDaily.favorites.stateholder

import com.example.passionDaily.quote.data.local.entity.QuoteEntity
import kotlinx.coroutines.flow.StateFlow

interface FavoritesStateHolder {
    val favoriteQuotes: StateFlow<List<QuoteEntity>>
    val isFavoriteLoading: StateFlow<Boolean>
    val error: StateFlow<String?>

    fun updateFavoriteQuotes(quotes: List<QuoteEntity>)
    fun updateIsFavoriteLoading(isLoading: Boolean)
    fun updateError(errorMessage: String?)
}