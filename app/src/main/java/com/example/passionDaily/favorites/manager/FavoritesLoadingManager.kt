package com.example.passionDaily.favorites.manager

import com.example.passionDaily.quote.data.local.entity.QuoteEntity
import com.example.passionDaily.favorites.usecase.LoadFavoritesUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FavoritesLoadingManager @Inject constructor(
    private val loadFavoritesUseCase: LoadFavoritesUseCase,
) {

    fun updateFavoriteQuotes(quotes: List<QuoteEntity>) {
        loadFavoritesUseCase.updateFavoriteQuotes(quotes)
    }

    fun updateIsFavoriteLoading(isLoading: Boolean) {
        loadFavoritesUseCase.updateIsFavoriteLoading(isLoading)
    }

    suspend fun getAllFavorites(currentUserId: String): Flow<List<QuoteEntity>> {
        return loadFavoritesUseCase.getAllFavorites(currentUserId)
    }

    fun checkIfQuoteIsFavorite(userId: String, quoteId: String, categoryId: Int): Flow<Boolean> {
        return loadFavoritesUseCase.checkIfQuoteIsFavorite(userId, quoteId, categoryId)
    }
}