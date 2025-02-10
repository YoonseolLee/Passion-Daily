package com.example.passionDaily.favorites.state

import com.example.passionDaily.favorites.base.FavoritesViewModelState
import com.example.passionDaily.quote.data.remote.model.Quote
import com.example.passionDaily.quotecategory.model.QuoteCategory
import kotlinx.coroutines.flow.StateFlow

interface FavoriteSelectionState : FavoritesViewModelState {
    override val selectedQuoteCategory: StateFlow<QuoteCategory>
    override val quotes: StateFlow<List<Quote>>
}