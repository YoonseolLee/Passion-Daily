package com.example.passionDaily.favorites.manager

import com.example.passionDaily.favorites.usecase.LoadFavoritesUseCase
import com.example.passionDaily.quote.data.local.entity.QuoteEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FavoritesLoadingManagerImpl @Inject constructor(
    private val loadFavoritesUseCase: LoadFavoritesUseCase,
) : FavoritesLoadingManager {

    override fun updateFavoriteQuotes(quotes: List<QuoteEntity>) {
        loadFavoritesUseCase.updateFavoriteQuotes(quotes)
    }

    override fun updateIsFavoriteLoading(isLoading: Boolean) {
        loadFavoritesUseCase.updateIsFavoriteLoading(isLoading)
    }

    override suspend fun getAllFavorites(): Flow<List<QuoteEntity>> {
        return loadFavoritesUseCase.getAllFavorites()
    }

    override fun checkIfQuoteIsFavorite(quoteId: String, categoryId: Int): Flow<Boolean> {
        return loadFavoritesUseCase.checkIfQuoteIsFavorite(quoteId, categoryId)
    }
}
