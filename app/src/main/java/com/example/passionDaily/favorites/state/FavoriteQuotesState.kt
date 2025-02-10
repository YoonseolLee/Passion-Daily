package com.example.passionDaily.favorites.state

import com.example.passionDaily.favorites.base.FavoritesViewModelState
import com.example.passionDaily.quote.data.local.entity.QuoteEntity
import kotlinx.coroutines.flow.StateFlow

interface FavoriteQuotesState : FavoritesViewModelState {
    override val favoriteQuotes: StateFlow<List<QuoteEntity>>
    override val currentFavoriteQuote: StateFlow<QuoteEntity?>
}