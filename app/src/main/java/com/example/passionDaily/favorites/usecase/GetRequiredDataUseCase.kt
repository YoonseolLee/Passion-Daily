package com.example.passionDaily.favorites.usecase

import com.example.passionDaily.quote.data.remote.model.Quote
import com.example.passionDaily.quotecategory.model.QuoteCategory
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject

class GetRequiredDataUseCase @Inject constructor() {
    fun getRequiredDataForAdd(
        currentUser: FirebaseUser?,
        selectedCategory: QuoteCategory?,
        quotes: List<Quote>,
        quoteId: String
    ): Triple<FirebaseUser, QuoteCategory, Quote> {
        val user = currentUser ?: throw IllegalStateException("No user logged in")
        val category = selectedCategory ?: throw IllegalStateException("No category selected")
        val quote = quotes.find { it.id == quoteId }
            ?: throw IllegalStateException("Quote not found: $quoteId")

        return Triple(user, category, quote)
    }
}