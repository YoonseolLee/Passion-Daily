package com.example.passionDaily.quote.domain.usecase

import com.example.passionDaily.quote.data.remote.model.Quote
import com.example.passionDaily.quote.domain.model.QuoteResult
import com.example.passionDaily.quote.stateholder.QuoteStateHolder
import com.example.passionDaily.quotecategory.model.QuoteCategory
import com.google.firebase.firestore.DocumentSnapshot
import javax.inject.Inject

class QuoteListManagementUseCase @Inject constructor(
    private val quoteStateHolder: QuoteStateHolder
) {

    suspend fun clearExistingQuotes() {
        quoteStateHolder.clearQuotes()
    }

    suspend fun addInitialQuotes(beforeQuotes: List<Quote>, targetQuote: Quote) {
        val allQuotes = beforeQuotes + targetQuote
        quoteStateHolder.addQuotes(allQuotes, true)
    }

    suspend fun addAfterQuotes(afterQuotesResult: QuoteResult) {
        if (afterQuotesResult.quotes.isNotEmpty()) {
            quoteStateHolder.addQuotes(afterQuotesResult.quotes, false)
        }
    }

    fun getUpdatedLastLoadedQuote(document: DocumentSnapshot?): DocumentSnapshot? {
        return document
    }

    suspend fun updateSelectedCategory(category: QuoteCategory) {
        quoteStateHolder.updateSelectedCategory(category)
    }
}