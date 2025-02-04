package com.example.passionDaily.favorites.usecase

import com.example.passionDaily.R
import com.example.passionDaily.favorites.data.remote.repository.RemoteFavoriteRepository
import com.example.passionDaily.resources.StringProvider
import com.example.passionDaily.quotecategory.model.QuoteCategory
import com.google.firebase.auth.FirebaseUser
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class SaveFavoritesToRemoteUseCase @Inject constructor(
    private val remoteFavoriteRepository: RemoteFavoriteRepository,
    private val stringProvider: StringProvider
) {
    suspend fun addFavoriteToFirestore(
        currentUser: FirebaseUser,
        quoteId: String,
        selectedCategory: QuoteCategory
    ) {
        val favoriteData = createFavoriteData(quoteId, selectedCategory)
        val newDocumentId = generateNewDocumentId(currentUser, selectedCategory)

        remoteFavoriteRepository.addFavoriteToFirestore(
            currentUser,
            newDocumentId,
            favoriteData
        )
    }

    private fun createFavoriteData(
        quoteId: String,
        selectedCategory: QuoteCategory
    ): HashMap<String, String> {
        val category = selectedCategory.getLowercaseCategoryId()
        return hashMapOf(
            stringProvider.getString(R.string.added_at) to getCurrentFormattedDateTime(),
            stringProvider.getString(R.string.quote_id) to quoteId,
            stringProvider.getString(R.string.category) to category
        )
    }

    private fun getCurrentFormattedDateTime(): String {
        return LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern(stringProvider.getString(R.string.datetime_format)))
    }

    private suspend fun generateNewDocumentId(
        currentUser: FirebaseUser,
        selectedCategory: QuoteCategory
    ): String {
        val category = selectedCategory.getLowercaseCategoryId()
        val lastQuoteNumber = remoteFavoriteRepository.getLastQuoteNumber(currentUser, category)
        val newQuoteNumber = String.format(
            stringProvider.getString(R.string.quote_number_format),
            lastQuoteNumber + 1
        )
        return stringProvider.getString(R.string.quote_id_prefix) + newQuoteNumber
    }
}