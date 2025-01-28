package com.example.passionDaily.quote.action

import com.example.passionDaily.quote.base.QuoteViewModelActions

interface QuoteNavigationActions : QuoteViewModelActions {
    override fun previousQuote()

    override fun nextQuote()

    override fun navigateToQuoteWithCategory(quoteId: String, category: String)
}