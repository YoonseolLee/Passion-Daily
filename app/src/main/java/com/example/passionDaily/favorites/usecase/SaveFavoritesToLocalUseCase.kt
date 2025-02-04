package com.example.passionDaily.favorites.usecase

import com.example.passionDaily.favorites.data.local.entity.FavoriteEntity
import com.example.passionDaily.quotecategory.data.local.entity.QuoteCategoryEntity
import com.example.passionDaily.quote.data.local.entity.QuoteEntity
import com.example.passionDaily.quote.data.remote.model.Quote
import com.example.passionDaily.favorites.data.local.repository.LocalFavoriteRepository
import com.example.passionDaily.quotecategory.data.local.repository.LocalQuoteCategoryRepository
import com.example.passionDaily.quote.data.local.repository.LocalQuoteRepository
import com.example.passionDaily.quotecategory.model.QuoteCategory
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject

class SaveFavoritesToLocalUseCase @Inject constructor(
    private val localFavoriteRepository: LocalFavoriteRepository,
    private val localQuoteRepository: LocalQuoteRepository,
    private val localQuoteCategoryRepository: LocalQuoteCategoryRepository
) {

    suspend fun saveToLocalDatabase(
        currentUser: FirebaseUser,
        selectedCategory: QuoteCategory,
        currentQuote: Quote
    ) {
        ensureCategoryExists(selectedCategory)
        ensureQuoteExists(selectedCategory, currentQuote)
        saveFavorite(currentUser, currentQuote.id, selectedCategory)
    }

    private suspend fun ensureCategoryExists(category: QuoteCategory) {
        if (!localQuoteCategoryRepository.isCategoryExists(category.ordinal)) {
            val categoryEntity = QuoteCategoryEntity(
                categoryId = category.ordinal,
                categoryName = category.getLowercaseCategoryId()
            )
            localQuoteCategoryRepository.insertCategory(categoryEntity)
        }
    }

    private suspend fun ensureQuoteExists(
        category: QuoteCategory,
        quote: Quote
    ) {
        if (!localQuoteRepository.isQuoteExistsInCategory(quote.id, category.ordinal)) {
            val quoteEntity = QuoteEntity(
                quoteId = quote.id,
                text = quote.text,
                person = quote.person,
                imageUrl = quote.imageUrl,
                categoryId = category.ordinal
            )
            localQuoteRepository.insertQuote(quoteEntity)
        }
    }

    private suspend fun saveFavorite(
        user: FirebaseUser,
        quoteId: String,
        category: QuoteCategory
    ) {
        val favoriteEntity = FavoriteEntity(
            userId = user.uid,
            quoteId = quoteId,
            categoryId = category.ordinal
        )
        localFavoriteRepository.insertFavorite(favoriteEntity)
    }
}