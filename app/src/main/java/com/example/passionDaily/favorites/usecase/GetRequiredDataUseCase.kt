package com.example.passionDaily.favorites.usecase

import com.example.passionDaily.R
import com.example.passionDaily.quote.data.remote.model.Quote
import com.example.passionDaily.quotecategory.model.QuoteCategory
import com.example.passionDaily.resources.StringProvider
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject

class GetRequiredDataUseCase @Inject constructor(
    private val stringProvider: StringProvider
) {
    fun getRequiredDataForAdd(
        selectedCategory: QuoteCategory?,
        quotes: List<Quote>,
        quoteId: String
    ): Pair<QuoteCategory, Quote> {
        val category = selectedCategory ?: throw IllegalStateException(
            stringProvider.getString(R.string.error_no_category_selected)
        )
        val quote = quotes.find { it.id == quoteId }
            ?: throw IllegalStateException(
                stringProvider.getString(R.string.error_quote_not_found, quoteId)
            )

        return Pair(category, quote)
    }
}