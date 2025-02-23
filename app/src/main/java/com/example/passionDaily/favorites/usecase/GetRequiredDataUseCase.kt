package com.example.passionDaily.favorites.usecase

import com.example.passionDaily.quote.data.remote.model.Quote
import com.example.passionDaily.quotecategory.model.QuoteCategory
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject

class GetRequiredDataUseCase @Inject constructor() {
    fun getRequiredDataForAdd(
        selectedCategory: QuoteCategory?,
        quotes: List<Quote>,
        quoteId: String
    ): Pair<QuoteCategory, Quote> {
        val category = selectedCategory ?: throw IllegalStateException("No category selected")
        val quote = quotes.find { it.id == quoteId }
            ?: throw IllegalStateException("Quote not found: $quoteId")

        return Pair(category, quote)
    }
}