package com.example.passionDaily.favorites.usecase

import com.example.passionDaily.quote.data.local.entity.QuoteEntity
import com.example.passionDaily.favorites.data.local.repository.LocalFavoriteRepository
import com.example.passionDaily.favorites.stateholder.FavoritesStateHolder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LoadFavoritesUseCase @Inject constructor(
    private val favoritesStateHolder: FavoritesStateHolder,
    private val localFavoriteRepository: LocalFavoriteRepository,
) {
    fun updateIsFavoriteLoading(isLoading: Boolean) {
        favoritesStateHolder.updateIsFavoriteLoading(isLoading)
    }

    suspend fun getAllFavorites(userId: String): Flow<List<QuoteEntity>> {
        return localFavoriteRepository.getAllFavorites(userId)
    }

    fun updateFavoriteQuotes(quotes: List<QuoteEntity>) {
        favoritesStateHolder.updateFavoriteQuotes(quotes)
    }

    fun checkIfQuoteIsFavorite(
        userId: String,
        quoteId: String,
        categoryId: Int
    ): Flow<Boolean> {
        return localFavoriteRepository
            .checkFavoriteEntity(userId, quoteId, categoryId)
            .map { favorite ->
                // 즐겨찾기 엔티티가 존재하고, 모든 ID가 일치하는 경우에만 true 반환
                favorite?.run {
                    this.userId == userId &&
                            this.quoteId == quoteId &&
                            this.categoryId == categoryId
                } ?: false
            }
    }
}