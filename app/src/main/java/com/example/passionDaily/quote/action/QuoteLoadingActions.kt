package com.example.passionDaily.quote.action

import com.example.passionDaily.quote.base.QuoteViewModelActions
import com.example.passionDaily.util.QuoteCategory

interface QuoteLoadingActions : QuoteViewModelActions {
    override fun loadInitialQuotes(category: QuoteCategory?)
    override fun onCategorySelected(category: QuoteCategory?)
    override fun loadQuotes(category: QuoteCategory)
}