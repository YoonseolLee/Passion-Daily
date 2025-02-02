package com.example.passionDaily.quote.action

import android.content.Context
import com.example.passionDaily.quote.base.QuoteViewModelActions
import com.example.passionDaily.util.QuoteCategory

interface QuoteSharingActions : QuoteViewModelActions {
    override fun shareQuote(context: Context, imageUrl: String?, quoteText: String, author: String)
    override fun incrementShareCount(quoteId: String, category: QuoteCategory)
}