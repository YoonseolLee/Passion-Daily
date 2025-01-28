package com.example.passionDaily.quote.action

import android.content.Context
import com.example.passionDaily.util.QuoteCategory

interface QuoteViewModelActions {
    fun previousQuote()
    fun nextQuote()
    fun loadInitialQuotes(category: QuoteCategory?)
    fun onCategorySelected(category: QuoteCategory?)
    fun shareQuote(context: Context, imageUrl: String?, quoteText: String, author: String)
    fun incrementShareCount(quoteId: String, category: QuoteCategory?)
    fun navigateToQuoteWithCategory(quoteId: String, category: String)
    fun loadQuotes(category: QuoteCategory)
}