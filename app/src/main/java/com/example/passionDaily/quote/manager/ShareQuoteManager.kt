package com.example.passionDaily.quote.manager

import android.content.Context
import com.example.passionDaily.util.QuoteCategory

interface ShareQuoteManager {
    suspend fun shareQuote(
        context: Context,
        imageUrl: String?,
        quoteText: String,
        author: String
    )

    suspend fun incrementShareCount(quoteId: String, category: QuoteCategory?)
}