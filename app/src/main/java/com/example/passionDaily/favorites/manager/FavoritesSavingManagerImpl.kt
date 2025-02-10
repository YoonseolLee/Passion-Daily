package com.example.passionDaily.favorites.manager

import com.example.passionDaily.favorites.usecase.GetRequiredDataUseCase
import com.example.passionDaily.favorites.usecase.SaveFavoritesToLocalUseCase
import com.example.passionDaily.favorites.usecase.SaveFavoritesToRemoteUseCase
import com.example.passionDaily.quote.data.remote.model.Quote
import com.example.passionDaily.quotecategory.model.QuoteCategory
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject

class FavoritesSavingManagerImpl @Inject constructor(
    private val saveFavoritesToLocalUseCase: SaveFavoritesToLocalUseCase,
    private val saveFavoritesToRemoteUseCase: SaveFavoritesToRemoteUseCase,
    private val getRequiredDataUseCase: GetRequiredDataUseCase
) : FavoritesSavingManager {

    override suspend fun saveToLocalDatabase(
        currentUser: FirebaseUser,
        selectedCategory: QuoteCategory,
        currentQuote: Quote
    ) {
        saveFavoritesToLocalUseCase.saveToLocalDatabase(currentUser, selectedCategory, currentQuote)
    }

    override fun getRequiredDataForAdd(
        currentUser: FirebaseUser?,
        selectedCategory: QuoteCategory,
        quotes: List<Quote>,
        quoteId: String
    ): Triple<FirebaseUser, QuoteCategory, Quote>? {
        return getRequiredDataUseCase.getRequiredDataForAdd(
            currentUser,
            selectedCategory,
            quotes,
            quoteId
        )
    }

    override suspend fun addFavoriteToFirestore(
        currentUser: FirebaseUser,
        quoteId: String,
        selectedCategory: QuoteCategory
    ) {
        saveFavoritesToRemoteUseCase.addFavoriteToFirestore(currentUser, quoteId, selectedCategory)
    }
}