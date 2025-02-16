package com.example.passionDaily.favorites.stateholder

import com.example.passionDaily.quote.data.local.entity.QuoteEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FavoritesStateHolderImpl() : FavoritesStateHolder {
    private val _favoriteQuotes = MutableStateFlow<List<QuoteEntity>>(emptyList())
    override val favoriteQuotes: StateFlow<List<QuoteEntity>> = _favoriteQuotes.asStateFlow()

    private val _isFavoriteLoading = MutableStateFlow(false)
    override val isFavoriteLoading: StateFlow<Boolean> = _isFavoriteLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    override val error: StateFlow<String?> = _error.asStateFlow()

    private val optimisticFavorites = mutableSetOf<Pair<String, Int>>()

    override fun updateFavoriteQuotes(quotes: List<QuoteEntity>) {
        _favoriteQuotes.value = quotes
    }

    override fun updateIsFavoriteLoading(isLoading: Boolean) {
        _isFavoriteLoading.value = isLoading
    }

    override fun updateError(errorMessage: String?) {
        _error.value = errorMessage
    }

    override fun addOptimisticFavorite(quoteId: String, categoryId: Int) {
        optimisticFavorites.add(quoteId to categoryId)
    }

    override fun removeOptimisticFavorite(quoteId: String, categoryId: Int) {
        optimisticFavorites.remove(quoteId to categoryId)
    }

    override fun isOptimisticallyFavorite(quoteId: String, categoryId: Int): Boolean {
        return optimisticFavorites.contains(quoteId to categoryId)
    }

    override fun clearOptimisticFavorites() {
        optimisticFavorites.clear()
    }
}
