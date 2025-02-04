package com.example.passionDaily.quote.manager

import android.content.Context
import com.example.passionDaily.quotecategory.model.QuoteCategory

interface ShareQuoteManager {
    suspend fun shareQuote(
        context: Context,
        imageUrl: String?,
        quoteText: String,
        author: String
    )

    suspend fun incrementShareCount(quoteId: String, category: QuoteCategory?)
}