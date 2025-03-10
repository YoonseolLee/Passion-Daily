package com.example.passionDaily.quote.action

import com.example.passionDaily.quote.base.QuoteViewModelActions
import com.example.passionDaily.quotecategory.model.QuoteCategory

interface QuoteLoadingActions : QuoteViewModelActions {
    override fun loadQuotes(category: QuoteCategory)
    override fun navigateToQuoteWithCategory(quoteId: String, category: String)
}