package com.example.passionDaily.favorites.manager

import com.example.passionDaily.favorites.usecase.GetRequiredDataUseCase
import com.example.passionDaily.favorites.usecase.SaveFavoritesToLocalUseCase
import com.example.passionDaily.quote.data.remote.model.Quote
import com.example.passionDaily.quotecategory.model.QuoteCategory
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject

class FavoritesSavingManagerImpl @Inject constructor(
    private val saveFavoritesToLocalUseCase: SaveFavoritesToLocalUseCase,
    private val getRequiredDataUseCase: GetRequiredDataUseCase
) : FavoritesSavingManager {

    override suspend fun saveToLocalDatabase(
        selectedCategory: QuoteCategory,
        currentQuote: Quote
    ) {
        saveFavoritesToLocalUseCase.saveToLocalDatabase(selectedCategory, currentQuote)
    }

    override fun getRequiredDataForAdd(
        selectedCategory: QuoteCategory,
        quotes: List<Quote>,
        quoteId: String
    ): Pair<QuoteCategory, Quote>? {
        return getRequiredDataUseCase.getRequiredDataForAdd(
            selectedCategory,
            quotes,
            quoteId
        )
    }
}